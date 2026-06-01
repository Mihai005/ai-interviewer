package com.example.interviewer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StartInterviewRequest {

    @NotBlank(message = "Topic must not be blank")
    private String topic;
}
