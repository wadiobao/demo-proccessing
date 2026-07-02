package com.example.demo.modules.quiz.public_quiz.application;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.document.processing.api.DocumentProcessingFacade;
import com.example.demo.modules.quiz.generation.api.GenerationFacade;
import com.example.demo.modules.quiz.public_quiz.api.PublicQuizFacade;
import com.example.demo.modules.quiz.shared.domain.model.FileGenerateResponse;
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
        StateResponse<Object> response;
        String pdfText = null;

        if (config.getLevel() == 2) {
            try {
                pdfText = documentProcessingFacade.processDocument(file).getRawText();
                response = generationFacade.generatePersonalizedQuiz(List.of(pdfText), config, null);
            } catch (Exception e) {
                log.error("Personalized quiz generation failed: {}", e.getMessage());
                return StateResponse.builder().message("Failed to generate quiz").build();
            }
        } else {
            response = generationFacade.generateStandardQuiz(file, config, null);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            String username = authentication.getName();
            if (response.getResult() instanceof FileGenerateResponse) {
                try {
                    FileGenerateResponse fileResponse = (FileGenerateResponse) response.getResult();
                    if (pdfText == null) {
                        pdfText = fileResponse.getContentPdf();
                        if (pdfText == null || pdfText.isBlank()) {
                            pdfText = documentProcessingFacade.processDocument(file).getRawText();
                        }
                    }
                    fileResponse = generationFacade.persistQuiz(
                            fileResponse,
                            username,
                            file.getOriginalFilename(),
                            pdfText,
                            false,
                            config.getTopic(),
                            "PUBLIC"
                    );
                    response.setResult(fileResponse);
                } catch (Exception e) {
                    log.error("Failed to archive public quiz for user {}: {}", username, e.getMessage(), e);
                }
            }
        }

        return response;
    }
}
