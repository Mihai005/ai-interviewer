package com.example.interviewer.integration.groq;

import com.example.interviewer.integration.LlmConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroqRequest {

    private String model;
    private List<Message> messages;

    @Builder.Default
    private int max_tokens = LlmConstants.DEFAULT_MAX_TOKENS;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }
}
