package com.example.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.awt.Desktop;
import java.net.URI;

@SpringBootApplication
@EnableDiscoveryClient
public class UserServiceApplication {
    public static void main(String[] args) {
//        Desktop.isDesktopSupported();
        SpringApplication.run(UserServiceApplication.class, args);
    }

    /**
     * 启动完成后自动打开浏览器访问客服工作台页面
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        String url = "http://localhost:8082/agent.html";
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
                System.out.println("已自动打开浏览器: " + url);
            }
        } catch (Exception e) {
            System.out.println("无法自动打开浏览器，请手动访问: " + url);
        }
    }
} 