package com.example.demo.modules.quiz.generation.infrastructure.port;

import java.util.List;
import com.example.demo.modules.quiz.shared.domain.model.Question;

/**
 * Output port for AI-based question generation.
 */
public interface AiGenerationPort {
    
    /**
     * Generates a list of questions based on instructions and user prompt.
     */
    AiResponse generateQuestions(String instruction, String prompt);

    /**
     * Identifies and extracts questions from raw text.
     */
    AiResponse generateIdentifiedQuestions(String instruction, String prompt);

    /**
     * Regenerates questions (adaptive mode) based on existing content.
     */
    AiResponse reGenerateQuestions(String instruction, String prompt);

    /**
     * Generates an image based on a prompt for a specific question.
     */
    String[] generateImage(String prompt, int questionId);

    /**
     * Response wrapper for AI generation results.
     */
    record AiResponse(String status, List<Question> questions) {}
}
