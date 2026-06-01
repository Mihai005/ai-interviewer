package com.example.interviewer.exception;

import lombok.Getter;

@Getter
public class InterviewSessionNotFoundException extends RuntimeException {
    private final String sessionId;

    public InterviewSessionNotFoundException(String sessionId) {
        super(ExceptionMessages.INTERVIEW_SESSION_NOT_FOUND + sessionId);
        this.sessionId = sessionId;
    }
}

