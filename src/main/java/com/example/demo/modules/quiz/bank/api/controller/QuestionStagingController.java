package com.example.demo.modules.quiz.bank.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.quiz.bank.application.service.BulkQuestionUploadService;
import com.example.demo.modules.quiz.shared.domain.model.Question;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Controller for bulk question uploading and analysis.
 */
@RestController
@RequestMapping("/api/v1/quiz")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QuestionStagingController {

        BulkQuestionUploadService bulkQuestionUploadService;

        /**
         * Analyzes a file and stages questions in Redis for editing before final
         * commit.
         */
        @PostMapping("/analyze-questions")
        public ResponseEntity<StateResponse<Object>> uploadQuestions(
                        @RequestParam("file") MultipartFile file,
                        @RequestParam("sessionId") String sessionId) throws Exception {

                String username = SecurityContextHolder.getContext()
                                .getAuthentication().getName();

                List<Question> questions = bulkQuestionUploadService.stageQuestions(file, username, sessionId);

                return ResponseEntity.ok(StateResponse.builder()
                                .result(questions)
                                .message("Questions analyzed and staged successfully")
                                .build());
        }
}
