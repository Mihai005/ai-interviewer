package com.example.interviewer.domain;

import com.example.interviewer.dto.SummaryData;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static java.util.UUID.randomUUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewSession {
    private String id;
    private String topic;
    private List<Message> history;
    private Instant createdAt;
    private SummaryData summary;

    @Builder.Default
    private boolean completed = false;

    public static InterviewSession start(String topic) {
        return InterviewSession.builder()
                .id(randomUUID().toString())
                .topic(topic)
                .history(new ArrayList<>())
                .createdAt(Instant.now())
                .build();
    }
}
