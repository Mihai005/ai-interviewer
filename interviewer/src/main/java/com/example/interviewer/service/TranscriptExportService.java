package com.example.interviewer.service;

import com.example.interviewer.domain.InterviewSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranscriptExportService {

    @Value("${interview.transcript.export-dir:./transcripts}")
    private String exportDir;

    private final ObjectMapper mapper;

    public void export(InterviewSession session) {
        try {
            Path dir = Paths.get(exportDir);
            Files.createDirectories(dir);

            String safeTopic = session.getTopic()
                    .toLowerCase()
                    .replaceAll("[^a-z0-9]+", "_")
                    .replaceAll("^_|_$", "");
            safeTopic = safeTopic.substring(0, Math.min(40, safeTopic.length()));

            String timestamp = session.getCreatedAt()
                    .atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            String filename = "interview_%s_%s_%s.json".formatted(
                    safeTopic, timestamp, session.getId().substring(0, 8));

            mapper.writeValue(dir.resolve(filename).toFile(), session);
            log.info("Transcript saved: {}", filename);
        } catch (Exception e) {
            log.error("Failed to export transcript for session {}", session.getId(), e);
        }
    }
}
