package com.example.demo.mongo.service.quiz;

import org.springframework.stereotype.Component;

import com.example.demo.constants.Constants;
import com.example.demo.mongo.dto.quiz.QuizConfig;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Responsible for building AI prompts for quiz generation.
 * Follows Single Responsibility Principle.
 */
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QuizPromptBuilder  {

    /**
     * Builds a standard prompt for quiz generation.
     *
     * @param config  Quiz configuration
     * @param pdfText Extracted text from PDF
     * @return Formatted prompt string for AI
     */
    public String buildStandardPrompt(QuizConfig config, String pdfText) {
        return String.format(
                Constants.QuestionFormat.QUESTION_COUNT
                        + Constants.QuestionFormat.DIFFICULTY_LEVEL
                        + Constants.QuestionFormat.KNOWLEDGE_TYPE
                        + Constants.QuestionFormat.IMAGE_PRESENTATION
                        + Constants.QuestionFormat.LANGUAGE
                        + Constants.QuestionFormat.DOCUMENT_PROVIDED,
                config.getQuestionCount(),
                config.getLevel(),
                config.getType(),
                config.getImgQuest(),
                config.getLanguage(),
                pdfText);
    }

    /**
     * Builds a prompt for adaptive quiz regeneration (IRT-based).
     *
     * @param config  Quiz configuration with difficulty thresholds
     * @param pdfText Extracted text from PDF
     * @return Formatted prompt string for AI
     */
    public String buildRegenerationPrompt(QuizConfig config, String pdfText) {
        return String.format(
                Constants.QuestionFormat.QUESTION_COUNT
                        + Constants.QuestionFormat.MIN_DIFFICULT
                        + Constants.QuestionFormat.MAX_DIFFICULT
                        + Constants.QuestionFormat.LANGUAGE
                        + Constants.QuestionFormat.DOCUMENT_PROVIDED,
                config.getQuestionCount(),
                config.getMinDifficulty(),
                config.getMaxDifficulty(),
                config.getLanguage(),
                pdfText);
    }
}
