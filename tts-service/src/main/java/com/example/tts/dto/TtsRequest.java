package com.example.tts.dto;

import lombok.Data;

@Data
public class TtsRequest {
    private String text;
    private String voice = "alloy"; // 可选值：alloy, echo, fable, onyx, nova, shimmer
    private String model = "tts-1"; // 可选值：tts-1, tts-1-hd
} 