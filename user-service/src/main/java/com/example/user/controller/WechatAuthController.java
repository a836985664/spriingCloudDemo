package com.example.user.controller;

import com.example.user.dto.*;
import com.example.user.service.WechatAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 微信登录控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/auth/wechat")
@RequiredArgsConstructor
public class WechatAuthController {

    private final WechatAuthService wechatAuthService;

    /**
     * 获取二维码（PC端扫码登录）
     */
    @GetMapping("/qrcode")
    public QrcodeResponse getQrcode() {
        log.info("获取二维码");
        return wechatAuthService.generateQrcode();
    }

    /**
     * 查询登录状态（PC端轮询）
     */
    @GetMapping("/status")
    public LoginStatusResponse checkStatus(@RequestParam("state") String state) {
        log.info("查询登录状态，state: {}", state);
        return wechatAuthService.checkStatus(state);
    }

    /**
     * 获取微信授权URL（手机端点击登录）
     */
    @GetMapping("/auth-url")
    public AuthUrlResponse getAuthUrl() {
        log.info("获取微信授权URL");
        return wechatAuthService.getAuthUrl();
    }

    /**
     * 微信回调（两种登录方式都用）
     */
    @GetMapping("/callback")
    public LoginResponse callback(
            @RequestParam String code,
            @RequestParam String state
    ) {
        log.info("微信回调，code: {}, state: {}", code, state);
        return wechatAuthService.handleCallback(code, state);
    }

    /**
     * 模拟扫码（测试用）
     */
    @PostMapping("/mock-scan")
    public String mockScan(@RequestBody MockScanRequest request) {
        log.info("模拟扫码，state: {}", request.getState());
        return wechatAuthService.mockScan(request.getState());
    }
}
