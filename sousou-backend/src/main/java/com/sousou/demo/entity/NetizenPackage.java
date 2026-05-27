// 文件位置: src/main/java/com/sousou/demo/entity/NetizenPackage.java
package com.sousou.demo.entity;

public class NetizenPackage extends ServicePackage implements INetService {
    private int flow = 3 * 1024; // 3GB = 3072MB

    public NetizenPackage() {
        this.price = 68.0;
    }

    public int getFlow() { return flow; }
    public void setFlow(int flow) { this.flow = flow; }

    @Override
    public void showInfo() {
        System.out.println("网虫套餐：月租" + price + "元，包含流量" + flow + "MB。");
    }

    @Override
    public double net(int useFlow, ServicePackage currentPackage) {
        double cost = 0.0;
        if (useFlow > this.flow) {
            // 超出部分 0.1元/MB
            cost = (useFlow - this.flow) * 0.1;
            this.flow = 0; // 余量归零
        } else {
            this.flow -= useFlow; // 扣减余量
        }
        return cost; // 返回需要额外扣的钱
    }
}