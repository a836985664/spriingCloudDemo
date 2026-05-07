package com.example.user.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 语音录制控制器
 * 处理音频文件上传和保存
 */
@RestController
@RequestMapping("/api/voice")
@CrossOrigin(origins = "*")
public class VoiceController {

    @Value("${voice.upload.path:D:\\\\ideaProject\\\\spriingCloudDemo\\\\user-service\\\\src\\\\main\\\\resources}")
    private String uploadPath;

    /**
     * 上传音频文件
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadVoice(@RequestParam("file") MultipartFile file,
                                                          @RequestParam(value = "username", required = false) String username) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 检查文件是否为空
            if (file.isEmpty()) {
                result.put("success", false);
                result.put("message", "文件不能为空");
                return ResponseEntity.badRequest().body(result);
            }

            // 创建上传目录
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 生成文件名
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = (username != null ? username + "_" : "") + "voice_" + timestamp + extension;

            // 保存文件
            Path filePath = Paths.get(uploadPath, filename);
            Files.write(filePath, file.getBytes());

            // 返回结果
            result.put("success", true);
            result.put("message", "文件上传成功");
            result.put("filename", filename);
            result.put("path", filePath.toString());
            result.put("size", file.getSize());
            result.put("contentType", file.getContentType());
            result.put("format", extension.replace(".", ""));

            System.out.println("========================================");
            System.out.println("🎤 语音文件上传成功");
            System.out.println("文件名: " + filename);
            System.out.println("文件大小: " + file.getSize() + " 字节");
            System.out.println("文件格式: " + extension.replace(".", ""));
            System.out.println("保存路径: " + filePath);
            System.out.println("上传用户: " + (username != null ? username : "匿名"));
            System.out.println("========================================");

            return ResponseEntity.ok(result);

        } catch (IOException e) {
            System.err.println("❌ 文件上传失败: " + e.getMessage());
            result.put("success", false);
            result.put("message", "文件上传失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 获取上传的文件列表
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> listFiles() {
        Map<String, Object> result = new HashMap<>();

        try {
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                result.put("success", true);
                result.put("files", new String[0]);
                return ResponseEntity.ok(result);
            }

            File[] files = uploadDir.listFiles((dir, name) -> 
                name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".webm") || name.endsWith(".ogg")
            );

            String[] filenames = new String[files != null ? files.length : 0];
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    filenames[i] = files[i].getName();
                }
            }

            result.put("success", true);
            result.put("files", filenames);
            result.put("count", filenames.length);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("❌ 获取文件列表失败: " + e.getMessage());
            result.put("success", false);
            result.put("message", "获取文件列表失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
}
