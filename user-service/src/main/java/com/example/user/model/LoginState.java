package com.example.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录状态（内存存储）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginState {
    /**
     * 状态标识
     */
    private String state;

    /**
     * 登录状态：pending-待扫码，success-成功，expired-过期
     */
    private String status;

    /**
     * JWT Token
     */
    private String token;

    /**
     * 微信openid
     */
    private String openid;

    /**
     * 创建时间（用于判断是否过期）
     */
    private Long createTime;
}
