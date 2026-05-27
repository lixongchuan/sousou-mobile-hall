// 文件位置: src/main/java/com/sousou/demo/entity/INetService.java
package com.sousou.demo.entity;

public interface INetService {
    // 上网接口：返回额外产生的资费
    double net(int flow, ServicePackage currentPackage) ;
}