// 文件位置: src/main/java/com/sousou/demo/entity/ICallService.java
package com.sousou.demo.entity;

public interface ICallService {
    // 通话接口：传入通话分钟数和当前套餐，返回实际扣费金额
    double call(int minCount, ServicePackage currentPackage) ;
}