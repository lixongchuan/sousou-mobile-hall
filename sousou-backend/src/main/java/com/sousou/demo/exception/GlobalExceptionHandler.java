// 文件位置: src/main/java/com/sousou/demo/exception/GlobalExceptionHandler.java
package com.sousou.demo.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.HashMap;
import java.util.Map;

// 全局异常拦截器
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SousouException.class)
    public Map<String, Object> handleSousouException(SousouException e) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", 500); // 业务错误码
        map.put("error", e.getMessage()); // 将错误信息打包成JSON返回给前端
        return map;
    }
}