package com.example.demo.mongo.service;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.StateResponse;
import com.example.demo.mongo.dto.question.FileGenerateResponse;
import com.example.demo.mongo.dto.quiz.QuizConfig;
import com.example.demo.mongo.service.iservice.IQuizService;
import com.example.demo.mongo.service.quiz.QuizPersistenceManager;
import com.example.demo.mongo.service.quiz.QuizProcessor;

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

    /**
     * Processes a quiz for public (unauthenticated) users.
     * No data persistence occurs.
     *
     * @param file   PDF file to process
     * @param config Quiz configuration
     * @return StateResponse containing the generated quiz
     */
    @Override
	public StateResponse<Object> processPublicQuiz(MultipartFile file, QuizConfig config) {
        log.info("Processing public quiz request for file: {}", file.getOriginalFilename());
        return quizProcessor.processQuiz(file, config);
    }

    /**
     * Processes a quiz for authenticated users.
     * Persists quiz data if user is authenticated.
     *
     * @param file   PDF file to process
     * @param config Quiz configuration
     * @return StateResponse containing the generated quiz
     * @throws Exception if persistence fails
     */
    @Override
	public StateResponse<Object> processPrivateQuiz(MultipartFile file, QuizConfig config) throws Exception {
        log.info("Processing private quiz request for file: {}", file.getOriginalFilename());

        // Generate quiz
        StateResponse<Object> response = quizProcessor.processQuiz(file, config);
        FileGenerateResponse fileGenerateResponse = (FileGenerateResponse) response.getResult();
        // Persist data if user is authenticated
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info(authentication.toString());
        if (isAuthenticated(authentication)) {
            String username = authentication.getName();
            log.info("User {} is authenticated, persisting quiz data", username);

            fileGenerateResponse = persistenceManager.persistQuizData(
            		fileGenerateResponse,
                    username,
                    file.getOriginalFilename(),
                    fileGenerateResponse.getContentPdf());
        } else {
            log.debug("User not authenticated, skipping persistence");
        }

        response.setResult(fileGenerateResponse);
        
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
