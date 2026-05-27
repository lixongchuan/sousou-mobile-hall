// 文件位置: src/main/java/com/sousou/demo/entity/User.java
package com.sousou.demo.entity;

// 生成 getter/setter:
import java.util.ArrayList;
import java.util.List;


public class User {
    private String cardNumber; // 手机号 (主键)
    private String password;   // 密码
    private double balance;    // 余额
    private ServicePackage currentPackage; // 当前套餐 (多态体现)
    // 消费记录列表
    private List<ConsumeRecord> records = new ArrayList<>();

    // 必须保留无参构造，JSON反序列化需要
    public User() {}

    // Getter 和 Setter 
    public List<ConsumeRecord> getRecords() { return records; }
    public void setRecords(List<ConsumeRecord> records) { this.records = records; }
    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    public ServicePackage getCurrentPackage() { return currentPackage; }
    public void setCurrentPackage(ServicePackage currentPackage) { this.currentPackage = currentPackage; }
}