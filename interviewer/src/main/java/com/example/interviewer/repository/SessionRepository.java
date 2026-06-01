package com.example.interviewer.repository;

import com.example.interviewer.domain.InterviewSession;

import java.util.Optional;

public interface SessionRepository {
    void save(InterviewSession session);
    Optional<InterviewSession> findById(String id);
}
