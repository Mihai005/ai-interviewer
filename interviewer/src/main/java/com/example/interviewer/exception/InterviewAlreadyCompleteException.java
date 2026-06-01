package com.example.interviewer.exception;

import lombok.Getter;

@Getter
public class InterviewAlreadyCompleteException extends RuntimeException {
    private final String sessionId;

    public InterviewAlreadyCompleteException(String sessionId) {
        super(ExceptionMessages.INTERVIEW_ALREADY_COMPLETED + sessionId);
        this.sessionId = sessionId;
    }

}

