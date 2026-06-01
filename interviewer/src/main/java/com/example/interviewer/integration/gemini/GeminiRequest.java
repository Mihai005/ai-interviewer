package com.example.interviewer.integration.gemini;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@Builder
@Jacksonized
public class GeminiRequest {

    private SystemInstruction systemInstruction;
    private List<Content> contents;
    private GenerationConfig generationConfig;

    @Data
    @Builder
    @Jacksonized
    public static class SystemInstruction {
        private List<Part> parts;
    }

    @Data
    @Builder
    @Jacksonized
    public static class Content {
        private String role;
        private List<Part> parts;
    }

    @Data
    @Builder
    @Jacksonized
    public static class Part {
        private String text;
    }

    @Data
    @Builder
    @Jacksonized
    public static class GenerationConfig {
        private Integer maxOutputTokens;
    }
}
