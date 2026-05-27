// 文件位置: src/main/java/com/sousou/demo/exception/SousouException.java
package com.sousou.demo.exception;

// 自定义基础异常
public class SousouException extends RuntimeException {
    public SousouException(String message) {
        super(message);
    }
}