package com.example.demo.modules.quiz.generation.api;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.quiz.shared.domain.model.FileGenerateResponse;
import com.example.demo.modules.quiz.shared.domain.model.QuizConfig;

/**
 * Facade for Quiz Generation (Standard and Personalized).
 */
public interface GenerationFacade {
    /**
     * Executes standard quiz generation.
     */
    StateResponse<Object> generateStandardQuiz(MultipartFile file, QuizConfig config, String contentId);

    /**
     * Executes standard quiz generation with raw text.
     */
    StateResponse<Object> generateStandardQuiz(String text, String fileName, QuizConfig config, String contentId);

    /**
     * Executes personalized/adaptive quiz generation based on chunks.
     */
    StateResponse<Object> generatePersonalizedQuiz(List<String> chunks, QuizConfig config, String requestId);

    /**
     * Persists generated quiz data.
     * @throws Exception 
     */
    FileGenerateResponse persistQuiz(FileGenerateResponse response, String username, String fileName, String rawText) throws Exception;
    FileGenerateResponse persistQuiz(FileGenerateResponse response, String username, String fileName, String rawText, boolean shouldUpdateContentIds) throws Exception;
    FileGenerateResponse persistQuiz(FileGenerateResponse response, String username, String fileName, String rawText, boolean shouldUpdateContentIds, String explicitTopic) throws Exception;
}
