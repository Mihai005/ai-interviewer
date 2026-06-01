package com.example.interviewer.integration.gemini;

import com.example.interviewer.domain.Message;
import com.example.interviewer.exception.LlmResponseException;
import com.example.interviewer.integration.LlmClient;
import com.example.interviewer.integration.LlmConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class GeminiLlmClient implements LlmClient {

    private final RestClient restClient;
    private final String model;

    public GeminiLlmClient(
            @Value("${LLM_API_KEY}") String apiKey,
            @Value("${llm.model:gemini-flash-latest}") String model) {

        this.model = model;

        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta/models/")
                .defaultHeader("x-goog-api-key", apiKey)
                .defaultHeader("content-type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public String generateResponse(List<Message> history) {
        String systemText = history.stream()
                .filter(m -> LlmConstants.ROLE_SYSTEM.equals(m.getRole()))
                .map(Message::getContent)
                .findFirst()
                .orElse("");

        GeminiRequest.SystemInstruction systemInstruction = GeminiRequest.SystemInstruction.builder()
                .parts(List.of(GeminiRequest.Part.builder()
                        .text(systemText).build()
                        ))
                .build();

        List<GeminiRequest.Content> apiMessages = history.stream()
                .filter(m -> !m.getRole().equals(LlmConstants.ROLE_SYSTEM))
                .map(m -> {
                    String geminiRole = LlmConstants.ROLE_ASSISTANT.equals(m.getRole()) ?
                            "model" : LlmConstants.ROLE_USER;
                    return GeminiRequest.Content.builder()
                            .role(geminiRole)
                            .parts(List.of(GeminiRequest.Part.builder().text(m.getContent()).build()))
                            .build();
                })
                .toList();

        GeminiRequest requestPayload = GeminiRequest.builder()
                .systemInstruction(systemInstruction)
                .contents(apiMessages)
                .generationConfig(GeminiRequest.GenerationConfig.builder()
                        .maxOutputTokens(LlmConstants.DEFAULT_MAX_TOKENS).build())
                .build();

        GeminiResponse response = restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(this.model + ":generateContent")
                        .build())
                .body(requestPayload)
                .retrieve()
                .body(GeminiResponse.class);

        if (response != null && response.getCandidates() != null && !response.getCandidates().isEmpty()) {
            return response.getCandidates().getFirst()
                    .getContent()
                    .getParts()
                    .getFirst()
                    .getText();
        }

        throw new LlmResponseException(
                LlmResponseException.Provider.GEMINI,
                "Received empty response from Gemini API");
    }
}
