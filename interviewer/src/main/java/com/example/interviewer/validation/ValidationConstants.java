package com.example.interviewer.validation;

public final class ValidationConstants {

    public static final String TOPIC_MUST_NOT_BE_BLANK = "Topic must not be blank";
    public static final int TOPIC_MAX_LENGTH_VALUE = 100;
    public static final String TOPIC_MAX_LENGTH = "Topic exceeds the maximum length of " + TOPIC_MAX_LENGTH_VALUE +
            " characters.";
    public static final String ANSWER_MUST_NOT_BE_BLANK = "Answer must not be blank";
    public static final int ANSWER_MAX_LENGTH_VALUE = 500;
    public static final String ANSWER_MAX_SIZE = "Answer exceeds the maximum length of " + ANSWER_MAX_LENGTH_VALUE +
            " characters.";

    private ValidationConstants() {

    }
}
