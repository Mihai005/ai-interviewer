package com.example.interviewer.exception;

import lombok.Getter;

@Getter
public class InterviewAlreadyCompleteException extends RuntimeException {
    private final String sessionId;

    public InterviewAlreadyCompleteException(String sessionId) {
        super("Interview session is already complete: " + sessionId);
        this.sessionId = sessionId;
    }

}

