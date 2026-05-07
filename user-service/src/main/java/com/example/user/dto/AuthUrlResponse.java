package com.example.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 微信授权URL响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthUrlResponse {
    /**
     * 微信授权URL
     */
    private String authUrl;
}
