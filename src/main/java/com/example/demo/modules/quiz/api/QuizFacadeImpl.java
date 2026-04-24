package com.example.demo.modules.quiz.api;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.document.metadata.api.DocumentMetadataFacade;
import com.example.demo.modules.document.metadata.domain.model.DocumentMetadata;
import com.example.demo.modules.document.processing.api.DocumentProcessingFacade;
import com.example.demo.modules.document.processing.application.service.DocumentSplitter;
import com.example.demo.modules.quiz.analytics.application.UserAnalyticsService;
import com.example.demo.modules.quiz.api.dto.QuizSubmissionRequest;
import com.example.demo.modules.quiz.evaluation.application.IRTCalculator;
import com.example.demo.modules.quiz.evaluation.application.SubmitQuizUseCase;
import com.example.demo.modules.quiz.generation.application.GeneratePersonalizedQuizUseCase;
import com.example.demo.modules.quiz.generation.application.GenerateStandardQuizUseCase;
import com.example.demo.modules.quiz.generation.application.PersistQuizUseCase;
import com.example.demo.modules.quiz.shared.domain.model.FileGenerateResponse;
import com.example.demo.modules.quiz.shared.domain.model.QuizConfig;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.UserResourceMongoEntity;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.repository.UserResourceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the QuizFacade.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QuizFacadeImpl implements QuizFacade {

    private final GenerateStandardQuizUseCase generateStandardQuizUseCase;
    private final GeneratePersonalizedQuizUseCase generatePersonalizedQuizUseCase;
    private final SubmitQuizUseCase submitQuizUseCase;
    private final PersistQuizUseCase persistQuizUseCase;
    private final IRTCalculator irtCalculator;
    private final UserAnalyticsService userAnalyticsService;
    private final UserResourceRepository userResourceRepository;
    private final DocumentProcessingFacade documentProcessingFacade;
    private final DocumentMetadataFacade documentMetadataFacade;
    private final DocumentSplitter documentSplitter;


    @Override
    public StateResponse<Object> generateQuiz(MultipartFile file, QuizConfig config) {
        if (config.getLevel() == 2) {
            try {
                String pdfText = documentProcessingFacade.processDocument(file).getRawText();
                return generatePersonalizedQuizUseCase.execute(pdfText, file.getOriginalFilename(), config, null);
            } catch (Exception e) {
                log.error("Personalized quiz generation failed: {}", e.getMessage());
                return StateResponse.builder().message("Failed to generate quiz").build();
            }
        }
        return generateStandardQuizUseCase.execute(file, config, null);
    }

    @Override
    public StateResponse<Object> generatePrivateQuiz(List<MultipartFile> files, QuizConfig config, String username) throws Exception {
        log.info("Processing private adaptive quiz for user: {} with {} files", username, files.size());

        if (files.isEmpty()) {
            throw new IllegalArgumentException("At least one file is required");
        }

        // 1. Extract text and split into chunks from all files
        List<String> allChunks = new ArrayList<>();
        StringBuilder fullTextBuilder = new StringBuilder();

        for (MultipartFile file : files) {
            String rawText = documentProcessingFacade.processDocument(file).getRawText();
            fullTextBuilder.append(rawText).append("\n\n");
            
            // Split into chunks with source tagging
            List<String> fileChunks = documentSplitter.split(rawText, file.getOriginalFilename());
            allChunks.addAll(fileChunks);
        }
        
        String aggregatedText = fullTextBuilder.toString();

        // 2. Uniform Sampling to stay within AI context limits while maintaining coverage
        // Dynamic sampling: ~1.5x chunks relative to questionCount for optimal context diversity
        int dynamicMaxChunks = Math.min(60, Math.max(15, (int) (config.getQuestionCount() * 1.5)));
        List<String> sampledChunks = performUniformSampling(allChunks, dynamicMaxChunks);
        log.info("Sampled {} chunks from a total of {} chunks (Target: {})", 
                 sampledChunks.size(), allChunks.size(), dynamicMaxChunks);

        // 3. Process Metadata & Topic
        DocumentMetadata metadata = documentMetadataFacade.findOrCreateMetadata(aggregatedText, username);
        String detectedTopic = (config.getTopic() != null && !config.getTopic().isBlank()) 
                ? config.getTopic().trim().toLowerCase() 
                : metadata.getTopic();
        config.setTopic(detectedTopic);

        // 4. IRT Calibration & Bloom Allocation
        UserResourceMongoEntity userResource = userResourceRepository.findByUserNameAndTopic(username, detectedTopic)
                .orElseGet(() -> UserResourceMongoEntity.builder()
                        .userName(username)
                        .topic(detectedTopic)
                        .theta(0.0)
                        .b(0.0)
                        .sessionSize(config.getQuestionCount())
                        .history(new ArrayList<>())
                        .contentIds(new ArrayList<>())
                        .build());

        // Sync sessionSize
        if (config.getQuestionCount() > 0) {
            userResource.setSessionSize(config.getQuestionCount());
        }
        userResource = userResourceRepository.save(userResource);

        if (config.getLevel() == 2) {
            config.setTheta(userResource.getTheta());

            // Fisher Information based Bloom Allocation (Combined total and details)
            int[] counts = irtCalculator.allocateQuestionsByBloom(userResource.getTheta(), userResource.getSessionSize(), 0.10);
            String allocationStr = String.format("%d questions (Remembering: %d, Understanding: %d, Applying: %d, Analyzing: %d, Evaluating: %d, Creating: %d)",
                    userResource.getSessionSize(), counts[0], counts[1], counts[2], counts[3], counts[4], counts[5]);
            
            config.setBloomAllocation(allocationStr);
            config.setQuestionCount(userResource.getSessionSize());

            // Difficulty boundaries
            double suggestedB = irtCalculator.suggestDifficultyB(userResource.getTheta(), 0.8);
            config.setMinDifficulty(Math.max(-3.0, suggestedB - 1.5));
            config.setMaxDifficulty(Math.min(3.0, suggestedB + 1.5));
            
            log.info("Adaptive IRT Config: theta={}, allocation={}", userResource.getTheta(), allocationStr);
        }

        // 5. Generate Quiz (Personalized flow)
        if (config.getLevel() < 2) {
            // Standard mode in private flow is only allowed for single files
            if (files.size() > 1) {
                throw new RuntimeException("Standard mode only supports single file generation.");
            }
            return generateStandardQuizUseCase.execute(aggregatedText, files.get(0).getOriginalFilename(), config, metadata.getId());
        }

        // Personalized flow for Level 2
        StateResponse<Object> response = generatePersonalizedQuizUseCase.execute(sampledChunks, config, metadata.getId());

        // 6. Persist results
        if (response.getResult() instanceof FileGenerateResponse) {
            FileGenerateResponse fileResponse = (FileGenerateResponse) response.getResult();
            String fileName = files.size() > 1 ? "Personalized_" + detectedTopic : files.get(0).getOriginalFilename();
            fileResponse = persistQuizUseCase.execute(fileResponse, username, fileName, aggregatedText);
            response.setResult(fileResponse);
        }

        return response;
    }

    private List<String> performUniformSampling(List<String> allChunks, int maxChunks) {
        if (allChunks.size() <= maxChunks) {
            return allChunks;
        }
        List<String> sampled = new ArrayList<>();
        double step = (double) (allChunks.size() - 1) / (maxChunks - 1);
        for (int i = 0; i < maxChunks; i++) {
            int index = (int) Math.round(i * step);
            sampled.add(allChunks.get(index));
        }
        return sampled;
    }

    @Override
    public StateResponse<Object> submitQuiz(QuizSubmissionRequest request, String username) {
        return submitQuizUseCase.execute(request, username);
    }

    @Override
    public StateResponse<Object> getUserStats(String username, String topic) {
        return userAnalyticsService.getUserStats(username, topic);
    }

    @Override
    public StateResponse<Object> getOverviewStats(String username) {
        return userAnalyticsService.getOverviewStats(username);
    }
}
