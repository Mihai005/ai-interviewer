package com.example.interviewer.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpServerErrorException.ServiceUnavailable.class)
    public ResponseEntity<@NonNull ErrorResponse> handleServiceUnavailable(HttpServerErrorException.ServiceUnavailable ex) {
        log.warn("Service unavailable error caught", ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ErrorResponse.of(ExceptionMessages.AI_HIGH_DEMAND));
    }

    @ExceptionHandler(HttpClientErrorException.TooManyRequests.class)
    public ResponseEntity<@NonNull ErrorResponse> handleTooManyRequests(HttpClientErrorException.TooManyRequests ex) {
        log.warn("Too many requests error caught", ex);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ErrorResponse.of(ExceptionMessages.API_RATE_LIMIT_EXCEEDED));
    }

    @ExceptionHandler(InterviewAlreadyCompleteException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleInterviewAlreadyComplete(InterviewAlreadyCompleteException ex) {
        log.warn("Interview already complete for session {}", ex.getSessionId(), ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(ExceptionMessages.INTERVIEW_ALREADY_COMPLETED));
    }

    @ExceptionHandler(InterviewSessionNotFoundException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleInterviewSessionNotFound(InterviewSessionNotFoundException ex) {
        log.warn("Interview session not found: {}", ex.getSessionId(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(ExceptionMessages.INTERVIEW_SESSION_NOT_FOUND));
    }

    @ExceptionHandler(InterviewStorageException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleInterviewStorageException(InterviewStorageException ex) {
        log.error("Interview storage error during {} for {}", ex.getOperation(), ex.getTarget(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(ExceptionMessages.FAILED_TO_ACCESS_INTERVIEW_STORAGE));
    }

    @ExceptionHandler(LlmResponseException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleLlmResponseException(LlmResponseException ex) {
        log.warn("LLM response error from {}", ex.getProvider(), ex);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ErrorResponse.of(ExceptionMessages.AI_SERVICE_INVALID_RESPONSE));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleRuntimeException(RuntimeException ex) {
        log.error("RuntimeException caught", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(ExceptionMessages.UNEXPECTED_ERROR_PREFIX + ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .findFirst()
                .orElse(ExceptionMessages.INVALID_REQUEST);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .findFirst()
                .orElse(ExceptionMessages.INVALID_REQUEST);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(message));
    }

    @Data
    @AllArgsConstructor
    public static class ErrorResponse {
        private String error;

        public static ErrorResponse of(String error) {
            return new ErrorResponse(error);
        }
    }
}
