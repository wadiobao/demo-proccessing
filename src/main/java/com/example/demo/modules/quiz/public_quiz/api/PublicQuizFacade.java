package com.example.demo.modules.quiz.public_quiz.api;

import org.springframework.web.multipart.MultipartFile;
import com.example.demo.dto.StateResponse;
import com.example.demo.modules.quiz.shared.domain.model.QuizConfig;

/**
 * Facade for Public Quiz operations.
 */
public interface PublicQuizFacade {
    /**
     * Coordinate quiz generation from a file and configuration (Public Mode).
     */
    StateResponse<Object> generateQuiz(MultipartFile file, QuizConfig config);
}
