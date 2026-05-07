package com.example.demo.modules.quiz.adaptive.application.command;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.document.metadata.api.DocumentMetadataFacade;
import com.example.demo.modules.document.metadata.domain.model.DocumentMetadata;
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
 * Command for generating adaptive personalized quizzes from multiple documents.
 * Orchestrates document processing, topic management, and quiz configuration.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GenerateAdaptiveQuizCommand {

    private final GenerationFacade generationFacade;
    private final DocumentMetadataFacade documentMetadataFacade;
    
    // Internal Services
    private final AdaptiveQuizDocumentService documentService;
    private final AdaptiveQuizTopicService topicService;
    private final AdaptiveQuizConfigService configService;

    @Transactional
    public StateResponse<Object> execute(List<MultipartFile> files, QuizConfig config, String username) throws Exception {
        log.info("Executing generate adaptive quiz command for user: {} with {} files", username, files.size());

        if (files.isEmpty()) {
            throw new IllegalArgumentException("At least one file is required");
        }

        // 1. Process Documents (Text extraction, Metadata, Sampling)
        ProcessedDocumentResult docResult = documentService.processDocuments(files, username, config.getQuestionCount());

        // 2. Create Aggregate Metadata for AI context
        DocumentMetadata aggregateMetadata = documentMetadataFacade.findOrCreateMetadata(
                docResult.getAggregatedText(), 
                username, 
                files.size() == 1 ? files.get(0).getOriginalFilename() : "Combined Document"
        );
        
        // 3. Determine Topic
        String detectedTopic = (config.getTopic() != null && !config.getTopic().isBlank()) 
                ? config.getTopic().trim().toLowerCase() 
                : (aggregateMetadata.getTopic() != null ? aggregateMetadata.getTopic() : docResult.getFirstDetectedTopic());
        
        config.setTopic(detectedTopic);

        // 4. Sync User Topic Resource
        UserResourceMongoEntity userResource = topicService.syncTopicResource(
                username, detectedTopic, docResult.getIndividualMetadataIds(), config.getQuestionCount());

        // 5. Prepare Personalized Config (Bloom, Difficulty)
        configService.preparePersonalizedConfig(config, userResource);

        // 6. Generate Quiz
        return generateAndPersist(files, config, username, docResult, aggregateMetadata, detectedTopic);
    }

    private StateResponse<Object> generateAndPersist(
            List<MultipartFile> files, 
            QuizConfig config, 
            String username, 
            ProcessedDocumentResult docResult, 
            DocumentMetadata aggregateMetadata, 
            String detectedTopic) throws Exception {
        
        if (config.getLevel() < 2) {
            if (files.size() > 1) {
                throw new RuntimeException("Standard mode only supports single file generation.");
            }
            return generationFacade.generateStandardQuiz(
                    docResult.getAggregatedText(), 
                    files.get(0).getOriginalFilename(), 
                    config, 
                    aggregateMetadata.getId()
            );
        }

        StateResponse<Object> response = generationFacade.generatePersonalizedQuiz(
                docResult.getSampledChunks(), 
                config, 
                aggregateMetadata.getId()
        );

        if (response.getResult() instanceof FileGenerateResponse) {
            FileGenerateResponse fileResponse = (FileGenerateResponse) response.getResult();
            String fileName = files.size() > 1 ? "Personalized_" + detectedTopic : files.get(0).getOriginalFilename();
            // If multiple files, don't add the aggregate metadata ID to the UserResource
            fileResponse = generationFacade.persistQuiz(
                    fileResponse, username, fileName, docResult.getAggregatedText(), files.size() <= 1);
            response.setResult(fileResponse);
        }

        return response;
    }
}
