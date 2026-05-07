package com.example.demo.modules.quiz.adaptive.application.service;

import org.springframework.stereotype.Service;

import com.example.demo.modules.quiz.evaluation.api.EvaluationFacade;
import com.example.demo.modules.quiz.shared.domain.model.QuizConfig;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.UserResourceMongoEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for preparing quiz configurations based on user mastery and Bloom's taxonomy.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdaptiveQuizConfigService {

    private final EvaluationFacade evaluationFacade;

    /**
     * Enhances the quiz config with personalized parameters (Level 2).
     */
    public void preparePersonalizedConfig(QuizConfig config, UserResourceMongoEntity userResource) {
        if (config.getLevel() != 2) {
            return;
        }

        log.info("Preparing personalized config for user mastery (theta): {}", userResource.getTheta());
        
        config.setTheta(userResource.getTheta());
        
        // Allocate questions by Bloom's taxonomy levels
        int[] counts = evaluationFacade.allocateQuestionsByBloom(userResource.getTheta(), userResource.getSessionSize(), 0.10);
        String allocationStr = String.format("%d questions (Remembering: %d, Understanding: %d, Applying: %d, Analyzing: %d, Evaluating: %d, Creating: %d)",
                userResource.getSessionSize(), counts[0], counts[1], counts[2], counts[3], counts[4], counts[5]);
        
        config.setBloomAllocation(allocationStr);
        config.setQuestionCount(userResource.getSessionSize());

        // Suggest difficulty range (IRT)
        double suggestedB = evaluationFacade.suggestDifficultyB(userResource.getTheta(), 0.8);
        config.setMinDifficulty(Math.max(-3.0, suggestedB - 1.5));
        config.setMaxDifficulty(Math.min(3.0, suggestedB + 1.5));
    }
}
