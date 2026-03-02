package com.example.demo.mongo.service;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.StateResponse;
import com.example.demo.mongo.dto.question.FileGenerateResponse;
import com.example.demo.mongo.dto.quiz.QuizConfig;
import com.example.demo.mongo.entity.Content;
import com.example.demo.mongo.entity.UserResource;
import com.example.demo.mongo.repository.UserResourceRepository;
import com.example.demo.mongo.service.iservice.IContentService;
import com.example.demo.mongo.service.iservice.IQuizService;
import com.example.demo.mongo.service.quiz.QuizPersistenceManager;
import com.example.demo.mongo.service.quiz.QuizProcessor;
import com.example.demo.mongo.service.quiz.processor.DocumentProcessorContext;
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
    DocumentProcessorContext documentProcessorFactory;
    IContentService iContentService;

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

        // 1. Extract text early
        String pdfText = documentProcessorFactory.getProcessor(file).extractText(file);

        // 2. Topic handling - Call ContentService to find metadata without saving yet
        // (One Search)
        Content metadata = iContentService.findOrCreateMetadata(pdfText, username);

        String detectedTopic = config.getTopic();
        if (detectedTopic == null || detectedTopic.trim().isEmpty()) {
            detectedTopic = metadata.getTopic();
        }

        final String finalTopic = detectedTopic.trim().toLowerCase();
        config.setTopic(finalTopic);

        // 3. Fetch or Create User Resource using detected Topic
        UserResource userResource = userResourceRepository.findByUserNameAndTopic(username, finalTopic)
                .orElseGet(() -> {
                    log.info("Cold start: Creating new UserResource for user: {}, topic: {}", username, finalTopic);
                    UserResource newUser = UserResource.builder()
                            .userName(username)
                            .topic(finalTopic)
                            .theta(0.0) // Average level
                            .build();
                    return userResourceRepository.save(newUser);
                });

        // 4. Adaptive Difficulty Adjustment (Level 2)
        if (config.getLevel() == 2) {
            log.info("Adaptive mode enabled. Current user theta: {} for topic: {}", userResource.getTheta(),
                    finalTopic);

            // Calculate suggested difficulty (b) targeting P_correct = 0.8
            double suggestedB = irtCalculator.suggestDifficultyB(userResource.getTheta(), 0.8);

            // Set dynamic thresholds with a +/- 0.5 buffer
            config.setMinDifficulty(suggestedB - 0.5);
            config.setMaxDifficulty(suggestedB + 0.5);

            log.info("Adjusted Adaptive Params: suggestedB={}, min={}, max={}",
                    suggestedB, config.getMinDifficulty(), config.getMaxDifficulty());
        }

        // 5. Generate quiz using pre-extracted text (Supports Hybrid Generation)
        StateResponse<Object> response = quizProcessor.processQuiz(file, pdfText, config, metadata.getId());

        if (response.getResult() instanceof FileGenerateResponse) {
            FileGenerateResponse fileGenerateResponse = (FileGenerateResponse) response.getResult();

            // 6. Persist quiz data (Save Content here ONLY if it's new - One Save)
            fileGenerateResponse = persistenceManager.persistQuizData(
                    fileGenerateResponse,
                    username,
                    file.getOriginalFilename(),
                    pdfText,
                    metadata);

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
