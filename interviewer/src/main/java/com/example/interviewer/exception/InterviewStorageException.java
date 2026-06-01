package com.example.interviewer.exception;

import lombok.Getter;

@Getter
public class InterviewStorageException extends RuntimeException {
    public enum Operation {
        INITIALIZE,
        SAVE,
        READ
    }

    private final Operation operation;
    private final String target;

    public InterviewStorageException(Operation operation, String target, String message, Throwable cause) {
        super(message, cause);
        this.operation = operation;
        this.target = target;
    }
}

