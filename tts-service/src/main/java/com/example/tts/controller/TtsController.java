package com.example.tts.controller;

import com.example.tts.dto.TtsRequest;
import com.example.tts.service.TtsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/")
@RequiredArgsConstructor
public class TtsController {

    private final TtsService ttsService;

    @PostMapping("tts")
    public ResponseEntity<byte[]> textToSpeech(@RequestBody TtsRequest request) {
        log.info("收到文本转语音请求: {}", request);
        try {
            if (request.getText() == null || request.getText().trim().isEmpty()) {
                log.error("文本内容为空");
                return ResponseEntity.badRequest().build();
            }

            byte[] audioData = ttsService.textToSpeech(request.getText(), request.getVoice(), request.getModel());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "speech.wav");

            log.info("文本转语音成功，音频大小: {} 字节", audioData.length);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(audioData);
        } catch (Exception e) {
            log.error("文本转语音失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}