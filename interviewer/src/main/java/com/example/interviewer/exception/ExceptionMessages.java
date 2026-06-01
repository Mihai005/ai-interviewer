package com.example.interviewer.exception;

public final class ExceptionMessages {

    public static final String AI_HIGH_DEMAND =
            "The AI is currently experiencing high demand. Please wait a moment and try again.";
    public static final String API_RATE_LIMIT_EXCEEDED =
            "API rate limit exceeded. Please slow down.";
    public static final String INTERVIEW_ALREADY_COMPLETED =
            "This interview has already been completed.";
    public static final String INTERVIEW_SESSION_NOT_FOUND =
            "Interview session not found.";
    public static final String FAILED_TO_ACCESS_INTERVIEW_STORAGE =
            "Failed to access interview storage.";
    public static final String AI_SERVICE_INVALID_RESPONSE =
            "The AI service returned an invalid response.";
    public static final String UNEXPECTED_ERROR_PREFIX =
            "An unexpected error occurred: ";
    public static final String INVALID_REQUEST =
            "Invalid request";
    public static final String EMPTY_RESPONSE_FROM_GEMINI_API =
            "Empty response from Gemini API";
    public static final String EMPTY_RESPONSE_FROM_GROQ_API =
            "Empty response from Groq API";

    private ExceptionMessages() {

    }
}
