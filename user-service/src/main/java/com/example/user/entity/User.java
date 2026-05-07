package com.example.user.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "sys_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;
    private String role;
    private String email;
    private Integer age;
    
    // 微信登录相关字段
    private String openid;      // 微信openid
    private String unionid;     // 微信unionid（预留）
    private String nickname;    // 微信昵称
    private String avatar;      // 微信头像
}