package com.example.interviewer.repository;

import com.example.interviewer.domain.InterviewSession;
import com.example.interviewer.exception.InterviewStorageException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Repository
public class FileSessionRepository implements SessionRepository {
    private final Path storagePath;
    private final ObjectMapper objectMapper;

    public FileSessionRepository(@Value("${STORAGE_PATH:./transcripts}") String storageDir) {
        this.storagePath = Paths.get(storageDir);

        this.objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();

        try {
            Files.createDirectories(this.storagePath);
        } catch (IOException e) {
            throw new InterviewStorageException(
                    InterviewStorageException.Operation.INITIALIZE,
                    this.storagePath.toString(),
                    "Could not initialize storage directory: " + this.storagePath,
                    e);
        }
    }

    @Override
    public void save(InterviewSession session) {
        try {
            File file = getFileForSession(session.getId());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, session);
        } catch (IOException e) {
            throw new InterviewStorageException(
                    InterviewStorageException.Operation.SAVE,
                    session.getId(),
                    "Failed to save session: " + session.getId(),
                    e);
        }
    }

    @Override
    public Optional<InterviewSession> findById(String id) {
        File file = getFileForSession(id);
        if (!file.exists()) {
            return Optional.empty();
        }
        try {
            InterviewSession session = objectMapper.readValue(file, InterviewSession.class);
            return Optional.of(session);
        } catch (IOException e) {
            throw new InterviewStorageException(
                    InterviewStorageException.Operation.READ,
                    id,
                    "Failed to read session: " + id,
                    e);
        }
    }

    private File getFileForSession(String id) {
        return storagePath.resolve(id + ".json").toFile();
    }
}
