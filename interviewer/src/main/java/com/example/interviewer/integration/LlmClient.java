package com.example.interviewer.integration;

import com.example.interviewer.domain.Message;

import java.util.List;

public interface LlmClient {
    String generateResponse(List<Message> history);
}
