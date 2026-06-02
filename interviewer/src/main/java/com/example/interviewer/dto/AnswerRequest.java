package com.example.interviewer.dto;

import com.example.interviewer.validation.ValidationConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnswerRequest {

    @NotBlank(message = ValidationConstants.ANSWER_MUST_NOT_BE_BLANK)
    @Size(max = ValidationConstants.ANSWER_MAX_LENGTH_VALUE, message = ValidationConstants.ANSWER_MAX_SIZE)
    private String answer;
}
