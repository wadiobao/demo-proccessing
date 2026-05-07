package com.example.demo.modules.quiz.public_quiz.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.quiz.public_quiz.api.PublicQuizFacade;
import com.example.demo.modules.quiz.shared.domain.model.QuizConfig;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Controller for public quiz generation (unauthenticated).
 */
@RestController
@RequestMapping("/api/v1/quiz")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublicQuizController {

    PublicQuizFacade publicQuizFacade;

    /**
     * Generates a quiz for public (unauthenticated) users.
     * No data is persisted.
     */
    @PostMapping("/public")
    public ResponseEntity<StateResponse<Object>> generatePublicQuiz(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "10") int questionCount,
            @RequestParam(defaultValue = "0") int level,
            @RequestParam(defaultValue = "0") int type,
            @RequestParam(defaultValue = "vietnamese") String language,
            @RequestParam(defaultValue = "0") int imgQuest) {

        QuizConfig config = QuizConfig.builder()
                .questionCount(questionCount)
                .level(level)
                .type(type)
                .language(language)
                .imgQuest(imgQuest)
                .build();

        StateResponse<Object> response = publicQuizFacade.generateQuiz(file, config);
        return ResponseEntity.ok(response);
    }
}
