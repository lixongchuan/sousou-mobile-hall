package com.sousou.demo.entity;

public class SupermanPackage extends ServicePackage implements ICallService, INetService, ISendService {
    private int talkTime = 200;  // 包含200分钟
    private int smsCount = 50;   // 包含50条短信
    private int flow = 1024;     // 包含1024MB流量 (1GB)

    public SupermanPackage() {
        this.price = 78.0; // 超人套餐月租 78 元
    }

    // Getter 和 Setter 必须有 (JSON序列化需要)
    public int getTalkTime() { return talkTime; }
    public void setTalkTime(int talkTime) { this.talkTime = talkTime; }
    public int getSmsCount() { return smsCount; }
    public void setSmsCount(int smsCount) { this.smsCount = smsCount; }
    public int getFlow() { return flow; }
    public void setFlow(int flow) { this.flow = flow; }

    @Override
    public void showInfo() {
        System.out.println("超人套餐：月租" + price + "元，包含通话" + talkTime + "分钟，短信" + smsCount + "条，流量" + flow + "MB。");
    }

    // 1. 打电话抵扣逻辑
    @Override
    public double call(int minCount, ServicePackage currentPackage) {
        double cost = 0.0;
        if (minCount > this.talkTime) {
            cost = (minCount - this.talkTime) * 0.2; // 超出部分 0.2元/分钟
            this.talkTime = 0;
        } else {
            this.talkTime -= minCount;
        }
        return cost;
    }

    // 2. 上网抵扣逻辑
    @Override
    public double net(int useFlow, ServicePackage currentPackage) {
        double cost = 0.0;
        if (useFlow > this.flow) {
            cost = (useFlow - this.flow) * 0.1; // 超出部分 0.1元/MB
            this.flow = 0;
        } else {
            this.flow -= useFlow;
        }
        return cost;
    }

    // 3. 发短信抵扣逻辑
    @Override
    public double send(int count, ServicePackage currentPackage) {
        double cost = 0.0;
        if (count > this.smsCount) {
            cost = (count - this.smsCount) * 0.1; // 超出部分 0.1元/条
            this.smsCount = 0;
        } else {
            this.smsCount -= count;
        }
        return cost;
    }
}