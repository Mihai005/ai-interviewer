package com.example.interviewer.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
                .body(ErrorResponse.of("The AI is currently experiencing high demand. Please wait a moment and try again."));
    }

    @ExceptionHandler(HttpClientErrorException.TooManyRequests.class)
    public ResponseEntity<@NonNull ErrorResponse> handleTooManyRequests(HttpClientErrorException.TooManyRequests ex) {
        log.warn("Too many requests error caught", ex);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ErrorResponse.of("API rate limit exceeded. Please slow down."));
    }

    @ExceptionHandler(InterviewAlreadyCompleteException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleInterviewAlreadyComplete(InterviewAlreadyCompleteException ex) {
        log.warn("Interview already complete for session {}", ex.getSessionId(), ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of("This interview has already been completed."));
    }

    @ExceptionHandler(InterviewSessionNotFoundException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleInterviewSessionNotFound(InterviewSessionNotFoundException ex) {
        log.warn("Interview session not found: {}", ex.getSessionId(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of("Interview session not found."));
    }

    @ExceptionHandler(InterviewStorageException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleInterviewStorageException(InterviewStorageException ex) {
        log.error("Interview storage error during {} for {}", ex.getOperation(), ex.getTarget(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of("Failed to access interview storage."));
    }

    @ExceptionHandler(LlmResponseException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleLlmResponseException(LlmResponseException ex) {
        log.warn("LLM response error from {}", ex.getProvider(), ex);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ErrorResponse.of("The AI service returned an invalid response."));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleRuntimeException(RuntimeException ex) {
        log.error("RuntimeException caught", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of("An unexpected error occurred: " + ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .findFirst()
                .orElse("Invalid request");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .findFirst()
                .orElse("Invalid request");
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
