package com.example.user.config;

import com.example.user.service.WechatAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务配置
 */
@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class ScheduledTasks {

    private final WechatAuthService wechatAuthService;

    /**
     * 每分钟清理一次过期的登录状态
     */
    @Scheduled(fixedRate = 60000)
    public void cleanExpiredLoginStates() {
        try {
            wechatAuthService.cleanExpiredStates();
        } catch (Exception e) {
            log.error("清理过期登录状态失败", e);
        }
    }
}
