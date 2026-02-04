package com.example.demo.mongo.service.iservice;

import com.example.demo.dto.StateResponse;
import com.example.demo.mongo.dto.quiz.QuizConfig;
import org.springframework.web.multipart.MultipartFile;

/**
 * Interface for quiz orchestration operations.
 */
public interface IQuizService {

    /**
     * Processes a quiz for public (unauthenticated) users.
     *
     * @param file   PDF file to process
     * @param config Quiz configuration
     * @return StateResponse containing the generated quiz
     */
    StateResponse<Object> processPublicQuiz(MultipartFile file, QuizConfig config);

    /**
     * Processes a quiz for authenticated users with data persistence.
     *
     * @param file   PDF file to process
     * @param config Quiz configuration
     * @return StateResponse containing the generated quiz
     * @throws Exception if processing or persistence fails
     */
    StateResponse<Object> processPrivateQuiz(MultipartFile file, QuizConfig config) throws Exception;
}
