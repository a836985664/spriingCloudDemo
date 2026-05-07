package com.example.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录状态响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginStatusResponse {
    /**
     * 状态：pending-待扫码，success-成功，expired-过期
     */
    private String status;

    /**
     * JWT Token（登录成功时返回）
     */
    private String token;
}
