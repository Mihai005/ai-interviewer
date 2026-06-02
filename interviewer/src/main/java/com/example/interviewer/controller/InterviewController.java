package com.example.interviewer.controller;

import com.example.interviewer.dto.AnswerRequest;
import com.example.interviewer.dto.InterviewResponse;
import com.example.interviewer.dto.StartInterviewRequest;
import com.example.interviewer.service.InterviewService;
import com.example.interviewer.validation.ValidationConstants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(ControllerConstants.API_INTERVIEWS_PATH)
public class InterviewController {
    private final InterviewService interviewService;

    @PostMapping("/start")
    public ResponseEntity<@NonNull InterviewResponse> startInterview(@Valid @RequestBody StartInterviewRequest request) {
        var response = interviewService.startInterview(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{sessionId}/answer")
    public ResponseEntity<@NonNull InterviewResponse> submitQuestion(
            @PathVariable String sessionId,
            @Valid @RequestBody AnswerRequest request) {

        var nextAiResponse = interviewService.processAnswer(sessionId, request);
        return ResponseEntity.ok(nextAiResponse);
    }
}
