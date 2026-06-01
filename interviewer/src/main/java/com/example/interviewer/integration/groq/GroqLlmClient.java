package com.example.interviewer.integration.groq;

import com.example.interviewer.domain.Message;
import com.example.interviewer.exception.ExceptionMessages;
import com.example.interviewer.exception.LlmResponseException;
import com.example.interviewer.integration.LlmClient;
import com.example.interviewer.integration.LlmConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Primary
@Component
public class GroqLlmClient implements LlmClient {

    private final RestClient restClient;
    private final String model;

    public GroqLlmClient(
            @Value("${LLM_API_KEY}") String apiKey,
            @Value("${llm.model:llama-3.3-70b-versatile}") String model) {

        this.model = model;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public String generateResponse(List<Message> history) {
        List<GroqRequest.Message> groqMessages = history.stream()
                .map(m -> GroqRequest.Message.builder()
                        .role(m.getRole())
                        .content(m.getContent())
                        .build())
                .toList();

        GroqRequest request = GroqRequest.builder()
                .model(this.model)
                .messages(groqMessages)
                .max_tokens(LlmConstants.DEFAULT_MAX_TOKENS)
                .build();

        GroqResponse response = restClient.post()
                .uri("/chat/completions")
                .body(request)
                .retrieve()
                .body(GroqResponse.class);

        if (response != null
                && response.getChoices() != null
                && !response.getChoices().isEmpty()) {
            return response.getChoices().getFirst().getMessage().getContent();
        }

        throw new LlmResponseException(
                LlmResponseException.Provider.GROQ,
                ExceptionMessages.EMPTY_RESPONSE_FROM_GROQ_API);
    }
}
