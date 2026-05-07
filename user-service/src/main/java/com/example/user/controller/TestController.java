package com.example.user.controller;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
public class TestController {

    @Value("${myapp.config.message:Default Message}")
    private String message;
    @PostConstruct // 添加此注解，在Bean初始化时执行
    public void init() {
        System.out.println("=========> Current message value: " + message);
    }
    @GetMapping("/config")
    public String getConfig() {
        return "Message: " + message;
    }
}