package com.example.interviewer.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InterviewResponse {
    private String sessionId;
    private String question;

    @Builder.Default
    private boolean completed = false;

    private SummaryData summary;
}
