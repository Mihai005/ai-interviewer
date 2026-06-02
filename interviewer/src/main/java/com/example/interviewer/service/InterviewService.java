package com.example.interviewer.service;

import com.example.interviewer.domain.InterviewSession;
import com.example.interviewer.domain.Message;
import com.example.interviewer.dto.AnswerRequest;
import com.example.interviewer.dto.InterviewResponse;
import com.example.interviewer.dto.StartInterviewRequest;
import com.example.interviewer.dto.SummaryData;
import com.example.interviewer.exception.InterviewAlreadyCompleteException;
import com.example.interviewer.exception.InterviewSessionNotFoundException;
import com.example.interviewer.integration.LlmClient;
import com.example.interviewer.integration.LlmConstants;
import com.example.interviewer.repository.SessionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewService {
    private final SessionRepository sessionRepository;
    private final PromptService promptService;
    private final LlmClient llmClient;
    private final TranscriptExportService transcriptExportService;
    private final ObjectMapper objectMapper;

    @Value("${interview.max-questions}")
    private int maxQuestions;

    public InterviewResponse startInterview(StartInterviewRequest request) {
        var topic = request.getTopic();
        var session = InterviewSession.start(topic);

        session.getHistory().add(promptService.buildSystemPrompt(topic));
        session.getHistory().add(Message.builder()
                .role(LlmConstants.ROLE_USER)
                .content(LlmConstants.START_INTERVIEW)
                .build()
            );

        String firstQuestion = llmClient.generateResponse(session.getHistory());
        session.getHistory().add(Message.builder()
                .role(LlmConstants.ROLE_ASSISTANT)
                .content(firstQuestion)
                .build()
            );
        sessionRepository.save(session);

        return InterviewResponse.builder()
                .sessionId(session.getId())
                .question(firstQuestion)
                .build();
    }

    public InterviewResponse processAnswer(String sessionId, AnswerRequest request) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new InterviewSessionNotFoundException(sessionId));

        if (session.isCompleted()) {
            throw new InterviewAlreadyCompleteException(sessionId);
        }

        String userAnswer = request.getAnswer();

        session.getHistory().add(Message.builder()
                .role(LlmConstants.ROLE_USER)
                .content(userAnswer)
                .build()
            );

        String nextResponse = llmClient.generateResponse(session.getHistory());
        session.getHistory().add(Message.builder()
                .role(LlmConstants.ROLE_ASSISTANT)
                .content(nextResponse)
                .build()
            );

        long questionCount = session.getHistory().stream()
                .filter(m -> LlmConstants.ROLE_ASSISTANT.equals(m.getRole()))
                .count() - 1;

        boolean isLastQuestion = questionCount >= maxQuestions;
        SummaryData summary = null;

        if (isLastQuestion) {
            List<Message> summaryHistory = new ArrayList<>(session.getHistory());
            summaryHistory.add(promptService.buildSummaryPrompt());
            String summaryRaw = llmClient.generateResponse(summaryHistory);

            summary = parseSummary(summaryRaw);
            session.setSummary(summary);
            session.setCompleted(true);
            transcriptExportService.export(session);
        }

        sessionRepository.save(session);

        return InterviewResponse.builder()
                .sessionId(sessionId)
                .question(nextResponse)
                .completed(isLastQuestion)
                .summary(summary)
                .build();
    }

    private SummaryData parseSummary(String raw) {
        try {
            String cleaned = raw
                    .replaceAll("(?s)^```json\\s*", "")
                    .replaceAll("(?s)\\s*```$", "")
                    .trim();
            return objectMapper.readValue(cleaned, SummaryData.class);
        } catch (Exception e) {
            log.error("Failed to parse summary JSON, returning fallback. Raw: {}", raw, e);
            return SummaryData.builder()
                    .overview(raw)
                    .sentiment("neutral")
                    .sentimentScore(0.0)
                    .build();
        }
    }
}
