package com.example.interviewer.exception;

import lombok.Getter;

@Getter
public class LlmResponseException extends RuntimeException {
    public enum Provider {
        GEMINI,
        GROQ
    }

    private final Provider provider;

    public LlmResponseException(Provider provider, String message) {
        super(message);
        this.provider = provider;
    }
}

