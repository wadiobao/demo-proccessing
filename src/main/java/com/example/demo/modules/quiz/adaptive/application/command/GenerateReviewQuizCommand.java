package com.example.demo.modules.quiz.adaptive.application.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.document.metadata.api.DocumentMetadataFacade;
import com.example.demo.modules.quiz.adaptive.application.dto.ProcessedDocumentResult;
import com.example.demo.modules.quiz.adaptive.application.service.AdaptiveQuizConfigService;
import com.example.demo.modules.quiz.adaptive.application.service.AdaptiveQuizDocumentService;
import com.example.demo.modules.quiz.adaptive.application.service.AdaptiveQuizTopicService;
import com.example.demo.modules.quiz.generation.api.GenerationFacade;
import com.example.demo.modules.quiz.shared.domain.model.FileGenerateResponse;
import com.example.demo.modules.quiz.shared.domain.model.QuizConfig;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.UserResourceMongoEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Command for generating review quizzes based on existing topic documents.
 * Orchestrates document retrieval, personalization, and quiz generation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GenerateReviewQuizCommand {

    private final GenerationFacade generationFacade;
    private final DocumentMetadataFacade documentMetadataFacade;
    
    // Internal Services
    private final AdaptiveQuizDocumentService documentService;
    private final AdaptiveQuizTopicService topicService;
    private final AdaptiveQuizConfigService configService;

    @Transactional
    public StateResponse<Object> execute(String topicId, QuizConfig config, String username, String requestId) throws Exception {
        log.info("Executing generate review quiz command for user: {}, topic: {}", username, topicId);

        // 1. Fetch & Validate Topic Resource
        UserResourceMongoEntity userResource = topicService.getTopicResource(topicId, username);
        
        if (userResource.getContentIds() == null || userResource.getContentIds().isEmpty()) {
            throw new IllegalArgumentException("Topic has no documents associated with it.");
        }

        // Use the saved session size from topic settings if available, otherwise fallback to 15
        if (config.getQuestionCount() <= 0) {
            int defaultSize = userResource.getSessionSize() > 0 ? userResource.getSessionSize() : 15;
            config.setQuestionCount(defaultSize);
        }

        // 2. Process existing documents from Metadata IDs
        ProcessedDocumentResult docResult = documentService.processFromMetadataIds(
                userResource.getContentIds(), config.getQuestionCount());

        config.setTopic(userResource.getTopic());

        // 4. Prepare Personalized Config (Bloom, Difficulty)
        configService.preparePersonalizedConfig(config, userResource);

        // 5. Generate and Persist Quiz
        return generateAndPersist(config, username, docResult, userResource.getTopic(), requestId);
    }

    private StateResponse<Object> generateAndPersist(
            QuizConfig config, 
            String username, 
            ProcessedDocumentResult docResult, 
            String detectedTopic,
            String requestId) throws Exception {
        
        StateResponse<Object> response = generationFacade.generatePersonalizedQuiz(
                docResult.getSampledChunks(), 
                config, 
                requestId
        );

        if (response.getResult() instanceof FileGenerateResponse) {
            FileGenerateResponse fileResponse = (FileGenerateResponse) response.getResult();
            String fileName = "Review_" + detectedTopic;
            
            // For review quizzes, we NEVER want to add the aggregate metadata ID back to the topic files list
            fileResponse = generationFacade.persistQuiz(
                    fileResponse, username, fileName, docResult.getAggregatedText(), false, detectedTopic);
            response.setResult(fileResponse);
        }

        return response;
    }
}
