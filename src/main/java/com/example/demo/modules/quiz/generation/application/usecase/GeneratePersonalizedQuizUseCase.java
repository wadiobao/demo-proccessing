package com.example.demo.modules.quiz.generation.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.document.retrieval.api.DocumentRetrievalFacade;
import com.example.demo.modules.quiz.shared.application.QuizPromptBuilder;
import com.example.demo.modules.quiz.shared.application.QuizResponseBuilder;
import com.example.demo.modules.quiz.shared.domain.model.FileGenerateResponse;
import com.example.demo.modules.quiz.shared.domain.model.Question;
import com.example.demo.modules.quiz.shared.domain.model.QuizConfig;
import com.example.demo.modules.quiz.shared.domain.port.AiGenerationPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * UseCase for Personalized Adaptive Quiz Generation (Level 2).
 * Supports multi-chunk knowledge base for IRT-based adaptive learning.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GeneratePersonalizedQuizUseCase {

    private final DocumentRetrievalFacade documentRetrievalFacade;
    private final AiGenerationPort aiGenerationPort;
    private final QuizPromptBuilder promptBuilder;
    private final QuizResponseBuilder responseBuilder;

    /**
     * Entry point for multi-chunk (multi-file) personalized quiz.
     */
    public StateResponse<Object> execute(List<String> chunks, QuizConfig config, String requestId) {
        try {
            log.info("Generating personalized adaptive quiz from {} chunks (Topic: {})", chunks.size(), config.getTopic());

            if (config.getLevel() < 2) {
                throw new RuntimeException("Personalized UseCase requires Level 2 (Adaptive).");
            }

            QuizPromptBuilder.PromptContext context = promptBuilder.buildPersonalizedPrompt(config, chunks);
            log.info("[AI-CALL] Generating personalized questions (Instruction length: {})", context.instruction().length());
            
            List<Question> questions = aiGenerationPort.reGenerateQuestions(context.instruction(), context.userMessage()).questions();
            log.info("[AI-RESULT] Generated {} personalized questions", questions.size());

            String[] wordAndPdf = documentRetrievalFacade.generateQuizDocuments(questions);
            if (wordAndPdf == null) {
                return responseBuilder.buildFileGenerationError();
            }

            FileGenerateResponse response = FileGenerateResponse.builder()
                    .questions(questions)
                    .wordBase64(wordAndPdf[0])
                    .pdfBase64(wordAndPdf[1])
                    .contentPdf(String.join("\n\n", chunks))
                    .topic(config.getTopic())
                    .requestId(requestId)
                    .build();

            return responseBuilder.buildSuccessResponse(response);

        } catch (Exception e) {
            log.error("Personalized quiz generation failed: {}", e.getMessage(), e);
            return responseBuilder.buildFileGenerationError();
        }
    }
}
