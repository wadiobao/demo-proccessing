package com.example.demo.modules.quiz.evaluation.api;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.quiz.adaptive.api.request.QuizSubmissionRequest;

/**
 * Facade for Quiz Evaluation and IRT calculations.
 */
public interface EvaluationFacade {
    /**
     * Executes the evaluation of a quiz submission.
     */
    StateResponse<Object> submitQuiz(QuizSubmissionRequest request, String username);

    /**
     * Suggests a difficulty level 'b' based on user's theta.
     */
    double suggestDifficultyB(double theta, double targetSuccessRate);

    /**
     * Allocates question counts across Bloom's levels based on user's theta.
     */
    int[] allocateQuestionsByBloom(double theta, int totalQuestions, double threshold);
}
