package com.example.interviewer;

import com.example.interviewer.domain.InterviewSession;
import com.example.interviewer.dto.InterviewResponse;
import com.example.interviewer.dto.StartInterviewRequest;
import com.example.interviewer.exception.InterviewAlreadyCompleteException;
import com.example.interviewer.integration.LlmClient;
import com.example.interviewer.integration.LlmConstants;
import com.example.interviewer.repository.SessionRepository;
import com.example.interviewer.domain.Message;
import com.example.interviewer.service.InterviewService;
import com.example.interviewer.service.PromptService;
import com.example.interviewer.service.TranscriptExportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterviewServiceTests {

    @Mock
    private SessionRepository sessionRepository;
    @Mock
    private PromptService promptService;
    @Mock
    private LlmClient llmClient;
    @Mock
    private TranscriptExportService transcriptExportService;

    private InterviewService interviewService;

    private static final String MOCK_QUESTION = "What programming languages do you know?";
    private static final String MOCK_SUMMARY_JSON = """
            {
              "overview": "Good candidate",
              "sentiment": "positive",
              "sentimentScore": 0.8,
              "themes": ["Java", "Spring"],
              "keyPoints": ["Knows Java well"],
              "keywords": ["Java", "Spring"]
            }
            """;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        interviewService = new InterviewService(
                sessionRepository, promptService, llmClient,
                transcriptExportService, objectMapper
        );

        ReflectionTestUtils.setField(interviewService, "maxQuestions", 4);

        lenient().when(promptService.buildSystemPrompt(anyString()))
                .thenReturn(Message.builder()
                        .role(LlmConstants.ROLE_SYSTEM)
                        .content("You are an interviewer.")
                        .build()
                    );
    }

    @Test
    void startInterview_shouldCreateSessionAndReturnFirstQuestion() {
        when(llmClient.generateResponse(anyList())).thenReturn(MOCK_QUESTION);

        StartInterviewRequest request = StartInterviewRequest.builder()
                .topic("Programming")
                .build();

        InterviewResponse response = interviewService.startInterview(request);

        assertThat(response.getSessionId()).isNotNull();
        assertThat(response.getQuestion()).isEqualTo(MOCK_QUESTION);
        assertThat(response.isCompleted()).isFalse();
        verify(sessionRepository).save(any(InterviewSession.class));
    }

    @Test
    void processAnswer_shouldReturnNextQuestionAndNotComplete() {
        when(llmClient.generateResponse(anyList())).thenReturn(MOCK_QUESTION);
        String sessionId = seedSession();

        InterviewResponse response = interviewService.processAnswer(sessionId, "Java");

        assertThat(response.getQuestion()).isEqualTo(MOCK_QUESTION);
        assertThat(response.isCompleted()).isFalse();
        assertThat(response.getSummary()).isNull();
    }

    @Test
    void processAnswer_onLastAnswer_shouldCompleteAndReturnSummary() {
        ReflectionTestUtils.setField(interviewService, "maxQuestions", 1);

        when(llmClient.generateResponse(anyList()))
                .thenReturn(MOCK_QUESTION)
                .thenReturn("Thank you, the interview is complete.")
                .thenReturn(MOCK_SUMMARY_JSON);

        String sessionId = seedSession();

        InterviewResponse response = interviewService.processAnswer(sessionId, "I use JUnit");

        assertThat(response.isCompleted()).isTrue();
        assertThat(response.getSummary()).isNotNull();
        assertThat(response.getSummary().getSentiment()).isEqualTo("positive");
        assertThat(response.getSummary().getSentimentScore()).isEqualTo(0.8);
        verify(transcriptExportService).export(any(InterviewSession.class));
    }

    @Test
    void processAnswer_shouldNotCompleteBeforeFourthAnswer() {
        when(llmClient.generateResponse(anyList())).thenReturn(MOCK_QUESTION);
        String sessionId = seedSession();

        for (int i = 1; i <= 3; i++) {
            InterviewResponse response = interviewService.processAnswer(sessionId, "answer " + i);
            assertThat(response.isCompleted())
                    .as("Should not be complete after answer %d", i)
                    .isFalse();
        }

        verify(transcriptExportService, never()).export(any());
    }

    @Test
    void processAnswer_onCompletedSession_shouldThrow() {
        when(llmClient.generateResponse(anyList())).thenReturn(MOCK_QUESTION);

        String sessionId = seedSession();
        InterviewSession session = sessionRepository.findById(sessionId).get();
        session.setCompleted(true);

        assertThatThrownBy(() -> interviewService.processAnswer(sessionId, "anything"))
                .isInstanceOf(InterviewAlreadyCompleteException.class);
    }

    private String seedSession() {
        InterviewSession[] saved = new InterviewSession[1];

        doAnswer(inv -> {
            saved[0] = inv.getArgument(0);
            return null;
        }).when(sessionRepository).save(any(InterviewSession.class));

        when(sessionRepository.findById(anyString()))
                .thenAnswer(inv -> Optional.ofNullable(saved[0]));

        StartInterviewRequest request = StartInterviewRequest.builder()
                .topic("Programming")
                .build();

        return interviewService.startInterview(request).getSessionId();
    }
}
