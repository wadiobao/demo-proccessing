package com.example.demo.modules.quiz.evaluation.api.internal;

import org.springframework.stereotype.Service;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.quiz.adaptive.api.request.QuizSubmissionRequest;
import com.example.demo.modules.quiz.evaluation.api.EvaluationFacade;
import com.example.demo.modules.quiz.evaluation.application.service.IRTCalculator;
import com.example.demo.modules.quiz.evaluation.application.usecase.SubmitQuizUseCase;

import lombok.RequiredArgsConstructor;

/**
 * Implementation of EvaluationFacade.
 */
@Service
@RequiredArgsConstructor
class EvaluationFacadeImpl implements EvaluationFacade {

    private final SubmitQuizUseCase submitQuizUseCase;
    private final IRTCalculator irtCalculator;

    @Override
    public StateResponse<Object> submitQuiz(QuizSubmissionRequest request, String username) {
        return submitQuizUseCase.execute(request, username);
    }

    @Override
    public double suggestDifficultyB(double theta, double targetSuccessRate) {
        return irtCalculator.suggestDifficultyB(theta, targetSuccessRate);
    }

    @Override
    public int[] allocateQuestionsByBloom(double theta, int totalQuestions, double threshold) {
        return irtCalculator.allocateQuestionsByBloom(theta, totalQuestions, threshold);
    }
}
