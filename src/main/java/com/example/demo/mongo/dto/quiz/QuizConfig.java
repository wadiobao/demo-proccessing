package com.example.demo.mongo.dto.quiz;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration object for quiz generation requests.
 * Encapsulates all parameters needed to generate a quiz from a PDF.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizConfig {

    /**
     * Number of questions to generate (1-50)
     */
    private int questionCount;

    /**
     * Difficulty level:
     * 0 = Easy
     * 1 = Hard
     * 2 = Adaptive (regeneration mode)
     */
    private int level;

    /**
     * Knowledge type:
     * 0 = Memorization
     * 1 = Application
     */
    private int type;

    /**
     * Language for questions (e.g., "vietnamese", "english")
     */
    private String language;

    /**
     * Whether to generate images for questions:
     * 0 = No images
     * 1 = Generate images
     */
    @Builder.Default
    private int imgQuest = 0;

    /**
     * For adaptive mode: minimum difficulty threshold
     */
    private Double minDifficulty;

    /**
     * For adaptive mode: maximum difficulty threshold
     */
    private Double maxDifficulty;
}
