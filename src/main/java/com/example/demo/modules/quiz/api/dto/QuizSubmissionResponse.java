package com.example.demo.modules.quiz.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for quiz submission results.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizSubmissionResponse {

    /**
     * Total number of questions
     */
    private int totalQuestions;

    /**
     * Number of correct answers
     */
    private int correctAnswers;

    /**
     * Score percentage (0-100)
     */
    private double scorePercentage;

    /**
     * Updated theta value (IRT ability parameter)
     */
    private double newTheta;

    /**
     * Updated difficulty parameter
     */
    private double newDifficulty;

    /**
     * Feedback message
     */
    private String feedback;
}
