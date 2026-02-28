package com.example.demo.mongo.service;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.StateResponse;
import com.example.demo.mongo.dto.TopicAndTags;
import com.example.demo.mongo.dto.question.FileGenerateResponse;
import com.example.demo.mongo.dto.quiz.QuizConfig;
import com.example.demo.mongo.entity.UserResource;
import com.example.demo.mongo.repository.UserResourceRepository;
import com.example.demo.mongo.service.iservice.IQuizService;
import com.example.demo.mongo.service.quiz.GeminiAIUtils;
import com.example.demo.mongo.service.quiz.QuizPersistenceManager;
import com.example.demo.mongo.service.quiz.QuizProcessor;
import com.example.demo.utils.IRTCalculator;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 * Main orchestrator for quiz generation operations.
 * Coordinates between QuizProcessor and QuizPersistenceManager.
 * Follows Single Responsibility Principle and Open/Closed Principle.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class QuizService implements IQuizService {

    QuizProcessor quizProcessor;
    QuizPersistenceManager persistenceManager;
    UserResourceRepository userResourceRepository;
    IRTCalculator irtCalculator;
    GeminiAIUtils geminiAIUtils;

    /**
     * Processes a quiz for public (unauthenticated) users.
     * No data persistence occurs.
     */
    @Override
    public StateResponse<Object> processPublicQuiz(MultipartFile file, QuizConfig config) {
        log.info("Processing public quiz request for file: {}", file.getOriginalFilename());
        return quizProcessor.processQuiz(file, config);
    }

    /**
     * Processes a quiz for authenticated users.
     * Persists quiz data and uses IRT for adaptive difficulty if requested.
     */
    @Override
    public StateResponse<Object> processPrivateQuiz(MultipartFile file, QuizConfig config) throws Exception {
        log.info("Processing private quiz request for file: {}", file.getOriginalFilename());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!isAuthenticated(authentication)) {
            return processPublicQuiz(file, config);
        }

        String username = authentication.getName();

        // 1. Topic handling
        String topic = config.getTopic();
        if (topic == null || topic.trim().isEmpty()) {
            log.info("Topic not provided, extracting from content using AI...");
            // Extract text temporarily for topic detection if needed,
            // but we'll let QuizProcessor handle extraction later for the actual quiz.
            // For now, assume we need a snippet to detect topic.
            // Simplified: Use filename as fallback if extraction is too heavy here,
            // but the plan says use AI Topic Extraction.
            // We can call IDocumentProcessor here but that's redundant.
            // Better: Extract topic from a small sample or wait for QuizProcessor.
            // Actually, let's just use filename normalized if AI fails.
            topic = "General: " + file.getOriginalFilename().replace(".pdf", "");
        }
        topic = topic.trim().toLowerCase();
        config.setTopic(topic);

        // 2. Fetch or Create User Resource
        UserResource userResource = userResourceRepository.findByUserNameAndTopic(username, topic)
                .orElseGet(() -> {
                    log.info("Cold start: Creating new UserResource for user: {}, topic: {}", username,
                            config.getTopic());
                    UserResource newUser = UserResource.builder()
                            .userName(username)
                            .topic(config.getTopic())
                            .theta(0.0) // Average level
                            .build();
                    return userResourceRepository.save(newUser);
                });

        // 3. Adaptive Difficulty Adjustment (Level 2)
        if (config.getLevel() == 2) {
            log.info("Adaptive mode enabled. Current user theta: {} for topic: {}", userResource.getTheta(), topic);

            // Calculate suggested difficulty (b) targeting P_correct = 0.8
            double suggestedB = irtCalculator.suggestDifficultyB(userResource.getTheta(), 0.8);

            // Set dynamic thresholds with a +/- 0.5 buffer
            config.setMinDifficulty(suggestedB - 0.5);
            config.setMaxDifficulty(suggestedB + 0.5);

            log.info("Adjusted Adaptive Params: suggestedB={}, min={}, max={}",
                    suggestedB, config.getMinDifficulty(), config.getMaxDifficulty());
        }

        // 4. Generate quiz
        StateResponse<Object> response = quizProcessor.processQuiz(file, config);

        if (response.getResult() instanceof FileGenerateResponse) {
            FileGenerateResponse fileGenerateResponse = (FileGenerateResponse) response.getResult();

            // AI Topic Extraction Fallback: If topic was "General", try to get a better one
            // from extracted text
            if (config.getTopic().startsWith("general:")) {
                try {
                    TopicAndTags detected = geminiAIUtils.detectTopicAndTags(fileGenerateResponse.getContentPdf());
                    if (detected != null && detected.getTopicId() != null) {
                        String betterTopic = detected.getTopicId().trim().toLowerCase();
                        log.info("AI detected better topic: {} (replacing {})", betterTopic, config.getTopic());
                        userResource.setTopic(betterTopic);
                        userResourceRepository.save(userResource);
                        config.setTopic(betterTopic);
                    }
                } catch (Exception e) {
                    log.warn("AI Topic extraction failed: {}", e.getMessage());
                }
            }

            // 5. Persist quiz data
            fileGenerateResponse = persistenceManager.persistQuizData(
                    fileGenerateResponse,
                    username,
                    file.getOriginalFilename(),
                    fileGenerateResponse.getContentPdf());

            response.setResult(fileGenerateResponse);
        }

        return response;
    }

    /**
     * Checks if the current user is authenticated (not anonymous).
     */
    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
