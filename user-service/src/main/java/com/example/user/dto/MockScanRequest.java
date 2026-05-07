package com.example.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模拟扫码请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MockScanRequest {
    /**
     * 状态标识
     */
    private String state;
}
