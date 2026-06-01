package com.example.interviewer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StartInterviewRequest {

    @NotBlank(message = "Topic must not be blank")
    private String topic;
}
