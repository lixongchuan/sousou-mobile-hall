// 文件位置: src/main/java/com/sousou/demo/dao/SousouDatabase.java
package com.sousou.demo.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sousou.demo.entity.User;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import java.nio.file.Files;
import java.nio.file.StandardCopyOption;


@Repository // 标记为数据访问层，交给Spring管理
public class SousouDatabase {

    // 全局唯一的用户内存表
    private ConcurrentHashMap<String, User> userMap = new ConcurrentHashMap<>();
    
    // JSON 数据存储路径 (放在项目根目录下)
    private static final String DATA_FILE = "sousou_users.json";
    
    // Spring Boot 内置了 Jackson，可以直接用
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 1. 项目启动时加载数据
    @PostConstruct
    public void initData() {
        File file = new File(DATA_FILE);
        if (file.exists() && file.length() > 0) {
            try {
                userMap = objectMapper.readValue(file, new TypeReference<ConcurrentHashMap<String, User>>() {});
                System.out.println("✅ 嗖嗖移动：数据加载成功，当前用户数: " + userMap.size());
            } catch (IOException e) {
                System.err.println("❌ 嗖嗖移动：数据加载失败！" + e.getMessage());
            }
        } else {
            System.out.println("⚠️ 嗖嗖移动：暂无历史数据，初始化空数据库。");
        }
    }

    // 2. 核心：每次数据改变都要调用此方法写入文件
    public synchronized void saveToFile() {
        File targetFile = new File(DATA_FILE);
        File tempFile = new File(DATA_FILE + ".tmp");
        try {
            // 1. 先写入临时文件
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(tempFile, userMap);
            
            // 2. 原子性替换原文件 (Atomic Move)
            Files.move(tempFile.toPath(), targetFile.toPath(), 
                       StandardCopyOption.REPLACE_EXISTING, 
                       StandardCopyOption.ATOMIC_MOVE);
                       
        } catch (IOException e) {
            System.err.println("❌ 嗖嗖移动：持久化失败！" + e.getMessage());
            // 尝试清理临时文件
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
    // 3. DAO 操作方法
    public void insertUser(User user) {
        userMap.put(user.getCardNumber(), user);
        saveToFile(); // 重点：内存写完，立马落盘到文件
    }

    public User selectUser(String cardNumber) {
        return userMap.get(cardNumber);
    }

    /**
     * 从内存Map中物理删除用户，并同步写入本地 JSON 文件 
     */
    public void deleteUser(String cardNumber) {
        userMap.remove(cardNumber);
        saveToFile(); // 物理删除后立刻落盘，保持数据一致性
    }

    public Map<String, User> getAllUsers() { return userMap; }
}