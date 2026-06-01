package com.example.interviewer.service;

import com.example.interviewer.domain.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PromptService {
    private final String systemPromptTemplate;
    private final String summaryPromptTemplate;

    @Value("${interview.max-questions}")
    private int maxQuestions;

    public PromptService(
            @Value("${llm.prompts.system}") String systemPromptTemplate,
            @Value("${llm.prompts.summary}") String summaryPromptTemplate) {
        this.systemPromptTemplate = systemPromptTemplate;
        this.summaryPromptTemplate = summaryPromptTemplate;
    }

    public Message buildSystemPrompt(String topic) {
        String formattedPrompt = String.format(systemPromptTemplate, topic, maxQuestions);

        return Message.builder().role("system").content(formattedPrompt).build();
    }

    public Message buildSummaryPrompt() {
        return Message.builder().role("user").content(summaryPromptTemplate).build();
    }
}
