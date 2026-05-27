// 文件位置: src/main/java/com/sousou/demo/entity/ConsumeRecord.java
package com.sousou.demo.entity;

public class ConsumeRecord {
    private String type; // 类型：通话、上网、短信
    private int amount;  // 消费量：分钟、MB、条
    private double cost; // 本次实际扣费(元)

    public ConsumeRecord() {}
    
    public ConsumeRecord(String type, int amount, double cost) {
        this.type = type;
        this.amount = amount;
        this.cost = cost;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }
    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }

}