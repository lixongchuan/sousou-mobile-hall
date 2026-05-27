package com.sousou.demo.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sousou.demo.dao.SousouDatabase;
import com.sousou.demo.entity.ConsumeRecord;
import com.sousou.demo.entity.ICallService;
import com.sousou.demo.entity.INetService;
import com.sousou.demo.entity.ISendService;
import com.sousou.demo.entity.NetizenPackage;
import com.sousou.demo.entity.ServicePackage;
import com.sousou.demo.entity.SupermanPackage;
import com.sousou.demo.entity.TalkativePackage;
import com.sousou.demo.entity.User;
import com.sousou.demo.exception.SousouException;

@Service
public class UserService {

    @Autowired
    private SousouDatabase database;

    /**
     * 业务1：注册开卡 
     */
    public User registerUser(String password, String packageType) {
        User user = new User();
        
        // ================== 核心防重改进 ==================
        String newPhone;
        Random random = new Random();
        do {
            // 生成 139 + 8位随机数字 (00000000 到 99999999)
            newPhone = "139" + String.format("%08d", random.nextInt(100000000));
        } while (database.selectUser(newPhone) != null);
        
        // 此时生成的 newPhone 绝对在系统里是独一无二的
        user.setCardNumber(newPhone);
        // =================================================

        user.setPassword(password);
        user.setBalance(100.0); // 新开卡默认赠送100元余额

        ServicePackage pack;
        switch (packageType) {
            case "TALK":
                pack = new TalkativePackage();
                break;
            case "NET":
                pack = new NetizenPackage();
                break;
            case "SUPER":
                pack = new SupermanPackage();
                break;
            default:
                throw new SousouException("暂不支持该套餐类型！只能是 TALK, NET, SUPER");
        }
        
        user.setCurrentPackage(pack);
        user.setBalance(user.getBalance() - pack.getPrice()); // 扣除首月月租

        database.insertUser(user);
        return user;
    }

    /**
     * 用户登录验证
     */
    public User login(String phone, String password) {
        User user = database.selectUser(phone);
        if (user == null) {
            throw new SousouException("该手机号未注册！");
        }
        if (!user.getPassword().equals(password)) {
            throw new SousouException("密码错误！");
        }
        return user; // 登录成功返回用户对象
    }

    /**
     * 业务2：模拟消费核心引擎
     * type: 1-打电话, 2-上网, 3-发短信
     */
    public String simulateConsume(String phone, int type, int amount) {
        User user = database.selectUser(phone);
        if (user == null) {
            throw new SousouException("该手机号不存在！");
        }

        ServicePackage pack = user.getCurrentPackage();
        double extraCost = 0.0;
        String consumeType = "";

        // 计费多态判断 (instanceof)
        switch (type) {
            case 1: // 拨打电话
                consumeType = "通话";
                if (pack instanceof ICallService) {
                    ICallService callPack = (ICallService) pack;
                    extraCost = callPack.call(amount, pack);
                } else {
                    // 没实现接口，全额按 0.2元/分 计费
                    extraCost = amount * 0.2;
                }
                break;
                
            case 2: // 上网冲浪
                consumeType = "上网";
                if (pack instanceof INetService) {
                    INetService netPack = (INetService) pack;
                    extraCost = netPack.net(amount, pack);
                } else {
                    // 没实现接口，全额按 0.1元/MB 计费
                    extraCost = amount * 0.1;
                }
                break;
                
            case 3: // 发送短信 
                consumeType = "短信";
                if (pack instanceof ISendService) {
                    ISendService sendPack = (ISendService) pack;
                    extraCost = sendPack.send(amount, pack);
                } else {
                    // 没实现接口，全额按 0.1元/条 计费
                    extraCost = amount * 0.1;
                }
                break;
                
            default:
                throw new SousouException("不支持的消费类型！必须是 1(通话), 2(上网), 3(短信)");
        }

        // 检查余额是否足够支付额外费用 (容错机制)
        if (user.getBalance() < extraCost) {
            throw new SousouException("余额不足！本次需扣除额外资费: " + extraCost + "元，当前余额仅剩: " + user.getBalance() + "元，请充值！");
        }

        // 执行扣款
        user.setBalance(user.getBalance() - extraCost);

        // 生成一条消费流水记录
        ConsumeRecord record = new ConsumeRecord(consumeType, amount, extraCost);
        user.getRecords().add(record);

        // 内存数据改完后，通知 DAO 重新覆盖到本地 JSON 文件！
        database.saveToFile();

        // 返回给前端的话术
        if (extraCost == 0.0) {
            return "消费成功！使用套餐内额度，未产生额外扣费。当前余额：" + user.getBalance() + "元";
        } else {
            return "消费成功！套餐内额度不足/不支持，本次产生额外资费：" + extraCost + "元，当前余额：" + user.getBalance() + "元";
        }
    }


    /**
     * 话费充值
     */
    public double recharge(String phone, double amount) {
        if (amount <= 0) {
            throw new SousouException("充值金额必须大于 0 元！");
        }
        User user = database.selectUser(phone);
        if (user == null) {
            throw new SousouException("该手机号不存在！");
        }
        
        user.setBalance(user.getBalance() + amount);
        database.saveToFile(); // 持久化
        return user.getBalance();
    }

    /**
     * 套餐余量查询
     * 由于套餐属性不同，我们需要根据不同套餐动态拼装余量信息
     */
    public Map<String, Object> getRemainingQuota(String phone) {
        User user = database.selectUser(phone);
        if (user == null) {
            throw new SousouException("该手机号不存在！");
        }

        ServicePackage pack = user.getCurrentPackage();
        Map<String, Object> quotaMap = new HashMap<>();
        quotaMap.put("packageName", pack.getClass().getSimpleName());
        quotaMap.put("balance", user.getBalance());

        // 使用 instanceof 严谨地识别多态属性
        if (pack instanceof TalkativePackage) {
            TalkativePackage tPack = (TalkativePackage) pack;
            quotaMap.put("remainingTalkTime", tPack.getTalkTime() + " 分钟");
            quotaMap.put("remainingSms", tPack.getSmsCount() + " 条");
        } else if (pack instanceof NetizenPackage) {
            NetizenPackage nPack = (NetizenPackage) pack;
            quotaMap.put("remainingFlow", nPack.getFlow() + " MB");
        } else if (pack instanceof SupermanPackage) {
            SupermanPackage sPack = (SupermanPackage) pack;
            quotaMap.put("remainingTalkTime", sPack.getTalkTime() + " 分钟");
            quotaMap.put("remainingSms", sPack.getSmsCount() + " 条");
            quotaMap.put("remainingFlow", sPack.getFlow() + " MB");
        }

        return quotaMap;
    }

    /**
     * 查询账单与消费清单
     */
    public List<ConsumeRecord> getConsumeRecords(String phone) {
        User user = database.selectUser(phone);
        if (user == null) {
            throw new SousouException("该手机号不存在！");
        }
        return user.getRecords();
    }

    /**
     * 套餐变更 (支持套餐升降级)
     * 规则：变更套餐需要立即扣除新套餐的月租，如果余额不足则变更失败
     */
    public String changePackage(String phone, String newPackageType) {
        User user = database.selectUser(phone);
        if (user == null) {
            throw new SousouException("该手机号不存在！");
        }

        ServicePackage oldPack = user.getCurrentPackage();
        String oldName = oldPack.getClass().getSimpleName();

        ServicePackage newPack;
        switch (newPackageType) {
            case "TALK":
                newPack = new TalkativePackage();
                break;
            case "NET":
                newPack = new NetizenPackage();
                break;
            case "SUPER":
                newPack = new SupermanPackage();
                break;
            default:
                throw new SousouException("无效的套餐类型！");
        }

        if (oldPack.getClass().equals(newPack.getClass())) {
            throw new SousouException("您当前已是该套餐，无需变更！");
        }

        // 校验余额是否足够扣除新套餐月租
        if (user.getBalance() < newPack.getPrice()) {
            throw new SousouException("余额不足以扣除新套餐月租 " + newPack.getPrice() + "元，变更失败！请先充值。");
        }

        // 变更扣费并替换套餐
        user.setBalance(user.getBalance() - newPack.getPrice());
        user.setCurrentPackage(newPack);
        
        database.saveToFile(); // 保存变更
        return "套餐变更成功！已从 " + oldName + " 变更为 " + newPack.getClass().getSimpleName() + "，已扣除新套餐月租: " + newPack.getPrice() + "元。";
    }

    /**
     * 办理退网 (销户)
     */
    public String cancelAccount(String phone) {
        User user = database.selectUser(phone);
        if (user == null) {
            throw new SousouException("该手机号不存在！");
        }
        
        double refund = user.getBalance();
        database.deleteUser(phone); // 物理删除
        
        return "退网成功！您的手机号 " + phone + " 已注销，退还话费余额: " + refund + "元。嗖嗖移动期待再次为您服务！";
    }


    @Autowired
    private SousouDatabase sousouDatabase; // 如果需要直接获取所有用户，可在DAO层提供暴露接口


    /**
     * 业务：订购流量加油包 (用例扩展)
     * size: 1-代表1GB, 3-代表3GB, 5-代表5GB
     */
    public String buyDataPack(String phone, int size) {
        User user = database.selectUser(phone);
        if (user == null) {
            throw new SousouException("该手机号不存在！");
        }

        ServicePackage pack = user.getCurrentPackage();

        // 1. 严谨校验：只有实现了 INetService (上网服务) 的套餐，才能订购流量加油包
        if (!(pack instanceof INetService)) {
            throw new SousouException("您当前使用的【" + pack.getClass().getSimpleName() + "】不支持上网服务，无法购买流量加油包！请先前往变更套餐。");
        }

        // 2. 根据前端点击的卡片，匹配价格与充入的MB数
        double price;
        int mbAmount;
        switch (size) {
            case 1:
                price = 10.0;
                mbAmount = 1024; // 1GB = 1024MB
                break;
            case 3:
                price = 20.0;
                mbAmount = 3072; // 3GB
                break;
            case 5:
                price = 30.0;
                mbAmount = 5120; // 5GB
                break;
            default:
                throw new SousouException("无效的流量包档位！只能选择 1GB, 3GB 或 5GB。");
        }

        // 3. 校验余额是否足够
        if (user.getBalance() < price) {
            throw new SousouException("话费余额不足！购买 " + size + "GB 流量包需 " + price + " 元，您当前余额仅剩: " + user.getBalance() + " 元，请先充值。");
        }

        // 4. 扣减话费
        user.setBalance(user.getBalance() - price);

        // 5. 增加套餐内的流量额度 (使用 instanceof 进行向下转型并操作)
        if (pack instanceof NetizenPackage) {
            NetizenPackage netPack = (NetizenPackage) pack;
            netPack.setFlow(netPack.getFlow() + mbAmount);
        } else if (pack instanceof SupermanPackage) {
            SupermanPackage superPack = (SupermanPackage) pack;
            superPack.setFlow(superPack.getFlow() + mbAmount);
        }

        // 6. 自动生成一条消费记录归档
        ConsumeRecord record = new ConsumeRecord("购买流量包", mbAmount, price);
        user.getRecords().add(record);

        // 7. 同步落盘保存
        database.saveToFile();

        return "订购成功！已扣除话费 " + price + " 元，已成功为您充入 " + size + " GB (共 " + mbAmount + "MB) 全国通用流量。";
    }

    /**
     * 模拟进入下一个月 (月结操作)
     * 逻辑：
     * 1. 遍历所有用户
     * 2. 扣除新一个月的月租
     * 3. 如果余额不足扣除，提示欠费停机 (这里我们可以直接扣负，代表欠费)
     * 4. 重置各套餐的初始额度
     * 5. 清空上个月的消费账单记录
     */
    public String simulateNextMonth() {
        // 为了简便，我们直接在 SousouDatabase 增加一个获取全局 Map 的方法：database.getAllUsers()
        Map<String, User> allUsers = database.getAllUsers();
        if (allUsers.isEmpty()) {
            return "当前系统中无注册用户，无需月结。";
        }

        int successCount = 0;
        int oweCount = 0;

        for (User user : allUsers.values()) {
            ServicePackage pack = user.getCurrentPackage();
            
            // 1. 扣除月租
            user.setBalance(user.getBalance() - pack.getPrice());
            if (user.getBalance() < 0) {
                oweCount++;
            } else {
                successCount++;
            }

            // 2. 重新初始化套餐额度 (通过重新实例化套餐对象实现，非常符合多态的设计)
            ServicePackage newPack;
            if (pack instanceof TalkativePackage) {
                newPack = new TalkativePackage();
            } else if (pack instanceof NetizenPackage) {
                newPack = new NetizenPackage();
            } else {
                newPack = new SupermanPackage();
            }
            user.setCurrentPackage(newPack);

            // 3. 清空上月详单
            user.getRecords().clear();
        }

        // 保存更新
        database.saveToFile();

        return "月结执行完毕！成功续费用户：" + successCount + " 户，续费后欠费停机用户：" + oweCount + " 户。所有 active 用户套餐额度已重置，历史详单已归档清空。";
    }
    

}