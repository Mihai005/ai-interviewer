package com.example.interviewer.exception;

import lombok.Getter;

@Getter
public class InterviewSessionNotFoundException extends RuntimeException {
    private final String sessionId;

    public InterviewSessionNotFoundException(String sessionId) {
        super("Session not found: " + sessionId);
        this.sessionId = sessionId;
    }
}

