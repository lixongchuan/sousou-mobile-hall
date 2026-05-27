package com.sousou.demo.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sousou.demo.entity.ConsumeRecord;
import com.sousou.demo.entity.User;
import com.sousou.demo.service.UserService;

import jakarta.servlet.http.HttpSession;


@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 注册开户
     */
    @PostMapping("/register")
    public Map<String, Object> register(@RequestParam String password, @RequestParam String type) {
        User newUser = userService.registerUser(password, type);
        Map<String, Object> res = new HashMap<>();
        res.put("code", 200);
        res.put("msg", "开户成功！");
        res.put("data", newUser);
        return res;
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Map<String, Object> login(
            @RequestParam String phone,
            @RequestParam String password,
            HttpSession session) {
            
        User user = userService.login(phone, password);
        
        // 将登录成功的手机号存入 Web Session 中
        session.setAttribute("currentUser", phone);
        
        Map<String, Object> res = new HashMap<>();
        res.put("code", 200);
        res.put("msg", "登录成功！欢迎进入嗖嗖营业厅。");
        res.put("data", user);
        return res;
    }

    /**
     * 用户退出登录
     */
    @PostMapping("/logout")
    public Map<String, Object> logout(HttpSession session) {
        session.removeAttribute("currentUser");
        Map<String, Object> res = new HashMap<>();
        res.put("code", 200);
        res.put("msg", "已安全退出登录。");
        return res;
    }

    /**
     * 模拟消费
     */
    @PostMapping("/consume")
    public Map<String, Object> consume(
            @RequestParam String phone,
            @RequestParam int type,
            @RequestParam int amount) {
        String resultMsg = userService.simulateConsume(phone, type, amount);
        Map<String, Object> res = new HashMap<>();
        res.put("code", 200);
        res.put("msg", resultMsg);
        return res;
    }

    /**
     * 话费充值
     */
    @PostMapping("/recharge")
    public Map<String, Object> recharge(@RequestParam String phone, @RequestParam double amount) {
        double newBalance = userService.recharge(phone, amount);
        Map<String, Object> res = new HashMap<>();
        res.put("code", 200);
        res.put("msg", "充值成功！当前最新余额为：" + newBalance + "元。");
        return res;
    }

    /**
     * 套餐余量与本月账单查询
     */
    @GetMapping("/quota")
    public Map<String, Object> getQuota(@RequestParam String phone) {
        Map<String, Object> quota = userService.getRemainingQuota(phone);
        Map<String, Object> res = new HashMap<>();
        res.put("code", 200);
        res.put("data", quota);
        return res;
    }


    /**
     * 接口：订购流量加油包
     */
    @PostMapping("/buy-data-pack")
    public Map<String, Object> buyDataPack(
            @RequestParam String phone,
            @RequestParam int size) {
            
        String msg = userService.buyDataPack(phone, size);
        Map<String, Object> res = new HashMap<>();
        res.put("code", 200);
        res.put("msg", msg);
        return res;
    }

    /**
     * 查询和打印消费详单
     */
    @GetMapping("/records")
    public Map<String, Object> getRecords(@RequestParam String phone) {
        List<ConsumeRecord> records = userService.getConsumeRecords(phone);
        Map<String, Object> res = new HashMap<>();
        res.put("code", 200);
        res.put("data", records);
        return res;
    }

    /**
     * 套餐变更
     */
    @PostMapping("/change-package")
    public Map<String, Object> changePackage(@RequestParam String phone, @RequestParam String type) {
        String msg = userService.changePackage(phone, type);
        Map<String, Object> res = new HashMap<>();
        res.put("code", 200);
        res.put("msg", msg);
        return res;
    }

    /**
     * 办理退网
     */
    @DeleteMapping("/cancel")
    public Map<String, Object> cancel(@RequestParam String phone) {
        String msg = userService.cancelAccount(phone);
        Map<String, Object> res = new HashMap<>();
        res.put("code", 200);
        res.put("msg", msg);
        return res;
    }

    /**
     * 查看资费说明 (静态配置信息接口)
     */
    @GetMapping("/tariffs")
    public Map<String, Object> getTariffs() {
        List<Map<String, Object>> list = new ArrayList<>();
        
        Map<String, Object> t1 = new HashMap<>();
        t1.put("name", "话唠套餐");
        t1.put("price", "58元/月");
        t1.put("detail", "包含500分钟通话，30条短信。超出计费：通话0.2元/分，短信0.1元/条。");
        
        Map<String, Object> t2 = new HashMap<>();
        t2.put("name", "网虫套餐");
        t2.put("price", "68元/月");
        t2.put("detail", "包含3GB(3072MB)流量。超出计费：流量0.1元/MB。");

        Map<String, Object> t3 = new HashMap<>();
        t3.put("name", "超人套餐");
        t3.put("price", "78元/月");
        t3.put("detail", "包含200分钟通话，50条短信，1GB(1024MB)流量。超出计费按各大标准计。");

        list.add(t1);
        list.add(t2);
        list.add(t3);

        Map<String, Object> res = new HashMap<>();
        res.put("code", 200);
        res.put("data", list);
        return res;
    }


    /**
     * 模拟月结 (进入下个月)
     */
    @PostMapping("/next-month")
    public Map<String, Object> nextMonth() {
        String msg = userService.simulateNextMonth();
        Map<String, Object> res = new HashMap<>();
        res.put("code", 200);
        res.put("msg", msg);
        return res;
    }
}