package com.example.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 二维码响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QrcodeResponse {
    /**
     * 二维码URL（前端可用此URL生成二维码）
     */
    private String qrcodeUrl;

    /**
     * 二维码图片（base64格式，前端可直接显示）
     */
    private String qrcodeImage;

    /**
     * 状态标识（用于轮询）
     */
    private String state;
}
