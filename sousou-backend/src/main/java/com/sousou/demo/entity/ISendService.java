// 文件位置: src/main/java/com/sousou/demo/entity/ISendService.java
package com.sousou.demo.entity;

public interface ISendService {
    // 发短信接口：返回额外产生的资费
    double send(int count, ServicePackage currentPackage) ;
}