package com.example.tts.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TtsService {

    @Value("${chattts.python.path}")
    private String pythonPath;

    @Value("${chattts.script.path}")
    private String scriptPath;

    @Value("${chattts.model.path}")
    private String modelPath;

    public byte[] textToSpeech(String text, String voice, String model) throws Exception {
        log.info("开始文本转语音处理");
        log.info("Python路径: {}", pythonPath);
        log.info("脚本路径: {}", scriptPath);
        log.info("模型路径: {}", modelPath);
        log.info("输入文本: {}", text);

        // 检查Python解释器是否存在
        File pythonFile = new File(pythonPath);
        if (!pythonFile.exists()) {
            throw new RuntimeException("Python解释器不存在: " + pythonPath);
        }

        // 检查脚本文件是否存在
        File scriptFile = new File(scriptPath);
        if (!scriptFile.exists()) {
            throw new RuntimeException("ChatTTS脚本不存在: " + scriptPath);
        }

        // 检查模型路径是否存在
        File modelDir = new File(modelPath);
        if (!modelDir.exists()) {
            throw new RuntimeException("模型目录不存在: " + modelPath);
        }


        // 创建临时目录用于存储生成的音频文件
        Path tempDir = Files.createTempDirectory("tts_");
        String outputFile = tempDir.resolve("output_audio_0.mp3").toString();
        log.info("输出文件路径: {}", outputFile);

        // 构建Python命令
        List<String> command = new ArrayList<>();
        command.add(pythonPath);
        command.add(scriptPath);
        command.add(text);
        command.add("--output");
        command.add(outputFile);
        command.add("--model-path");
        command.add(modelPath);

        log.info("执行命令: {}", String.join(" ", command));

        // 执行Python脚本
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        // 设置工作目录为模型所在目录
        processBuilder.directory(modelDir);
        processBuilder.environment().put("PYTHONIOENCODING", "utf-8");
        Process process = processBuilder.start();

        // 读取Python脚本的输出
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                log.info("Python输出: {}", line);
            }
        }

        // 等待Python脚本执行完成
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            String errorMessage = String.format("Python脚本执行失败，退出码: %d，输出: %s", exitCode, output);
            log.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }

        // 检查输出文件是否存在
        File outputAudioFile = new File(outputFile);
        if (!outputAudioFile.exists()) {
            throw new RuntimeException("音频文件未生成: " + outputFile);
        }

        // 读取生成的音频文件
        byte[] audioData = Files.readAllBytes(Paths.get(outputFile));
        log.info("音频文件大小: {} 字节", audioData.length);

        // 清理临时文件
        Files.deleteIfExists(Paths.get(outputFile));
        Files.deleteIfExists(tempDir);
        log.info("临时文件已清理");

        return audioData;
    }
}