package com.example.interviewer.dto;

import com.example.interviewer.validation.ValidationConstants;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StartInterviewRequest {

    @NotBlank(message = ValidationConstants.TOPIC_MUST_NOT_BE_BLANK)
    private String topic;
}
