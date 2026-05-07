package com.example.user.controller;

import com.example.user.service.SttRecognitionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/stt")
public class SttRecognitionController {

    private final SttRecognitionService sttRecognitionService;

    // 依赖注入，Spring Boot 会自动找到 Service 实例
    public SttRecognitionController(SttRecognitionService sttRecognitionService) {
        this.sttRecognitionService = sttRecognitionService;
    }

    /**
     * 接收前端上传的音频文件并调用 STT 识别服务。
     * 预期前端调用：POST /api/stt/recognize
     * @param file 上传的音频文件 (Blob -> MultipartFile)
     * @return 包含识别文本的响应体
     */
    @PostMapping("/recognize")
    public ResponseEntity<?> recognizeSpeech(@RequestParam("audioFile") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("未检测到上传的音频文件。");
        }
        
        // 1. 核心业务调用：将文件交给 Service 层处理
        try {
            String recognizedText = sttRecognitionService.processAudioFile(file);
            
            // 2. 成功返回识别的文本
            return ResponseEntity.ok(new SttResponse(true, "识别成功", recognizedText));
        } catch (Exception e) {
            // 捕获 Service 层抛出的所有业务异常
            e.printStackTrace();
            return ResponseEntity.status(500).body(new SttResponse(false, "后端处理失败", "处理过程中发生错误：" + e.getMessage()));
        }
    }

    // 简单的响应体类，方便前端解析
    public static class SttResponse {
        private boolean success;
        private String message;
        private String text;

        public SttResponse(boolean success, String message, String text) {
            this.success = success;
            this.message = message;
            this.text = text;
        }
        // Getters and Setters (省略，但实际项目中需要)
    }
}