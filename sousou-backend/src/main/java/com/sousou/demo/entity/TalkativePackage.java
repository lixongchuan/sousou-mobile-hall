// 文件位置: src/main/java/com/sousou/demo/entity/TalkativePackage.java
package com.sousou.demo.entity;

public class TalkativePackage extends ServicePackage implements ICallService, ISendService {
    private int talkTime = 500; // 套餐包含500分钟
    private int smsCount = 30;  // 套餐包含30条短信

    public TalkativePackage() {
        this.price = 58.0; // 话唠套餐58元
    }

    public int getTalkTime() { return talkTime; }
    public void setTalkTime(int talkTime) { this.talkTime = talkTime; }
    public int getSmsCount() { return smsCount; }
    public void setSmsCount(int smsCount) { this.smsCount = smsCount; }

    @Override
    public void showInfo() {
       System.out.println("话唠套餐：月租" + price + "元，包含通话" + talkTime + "分钟，短信" + smsCount + "条。");
    }

    // 在 TalkativePackage 类中，重写 call 方法：
    @Override
    public double call(int minCount, ServicePackage currentPackage) {
        double cost = 0.0;
        if (minCount > this.talkTime) {
            // 超出部分 0.2元/分钟
            cost = (minCount - this.talkTime) * 0.2;
            this.talkTime = 0;
        } else {
            this.talkTime -= minCount;
        }
        return cost;
    }

    // 发短信的抵扣逻辑
    @Override
    public double send(int count, ServicePackage currentPackage) {
        double cost = 0.0;
        if (count > this.smsCount) {
            cost = (count - this.smsCount) * 0.1; // 超出 0.1元/条
            this.smsCount = 0;
        } else {
            this.smsCount -= count;
        }
        return cost;
    }

}