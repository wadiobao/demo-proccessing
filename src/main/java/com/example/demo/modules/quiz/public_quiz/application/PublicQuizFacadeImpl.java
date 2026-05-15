package com.example.demo.modules.quiz.public_quiz.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.document.processing.api.DocumentProcessingFacade;
import com.example.demo.modules.quiz.generation.api.GenerationFacade;
import com.example.demo.modules.quiz.public_quiz.api.PublicQuizFacade;
import com.example.demo.modules.quiz.shared.domain.model.QuizConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of PublicQuizFacade.
 */
@Service
@RequiredArgsConstructor
@Slf4j
class PublicQuizFacadeImpl implements PublicQuizFacade {

    private final GenerationFacade generationFacade;
    private final DocumentProcessingFacade documentProcessingFacade;

    @Override
    public StateResponse<Object> generateQuiz(MultipartFile file, QuizConfig config) {
        if (config.getLevel() == 2) {
            try {
                String pdfText = documentProcessingFacade.processDocument(file).getRawText();
                return generationFacade.generatePersonalizedQuiz(List.of(pdfText), config, null);
            } catch (Exception e) {
                log.error("Personalized quiz generation failed: {}", e.getMessage());
                return StateResponse.builder().message("Failed to generate quiz").build();
            }
        }
        return generationFacade.generateStandardQuiz(file, config, null);
    }
}
