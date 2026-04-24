package com.example.demo.modules.quiz.shared.domain.model;

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
     * Topic of the quiz (used for tracking learning progress)
     */
    private String topic;

    /**
     * For adaptive mode: minimum difficulty threshold
     */
    private Double minDifficulty;

    /**
     * For adaptive mode: maximum difficulty threshold
     */
    private Double maxDifficulty;

    /**
     * IRT (Item Response Theory) ability estimate for personalized quiz generation.
     * Scale: -3.00 (novice) to +3.00 (expert).
     * Maps to Bloom's Taxonomy levels via the Theta Calibration Protocol.
     * Defaults to 0.0 (Applying level) for new users without response history.
     */
    @Builder.Default
    private Double theta = 0.0;

    /**
     * IRT-based distribution of questions per Bloom level.
     * Example: "Remembering=2, Understanding=5, Applying=10, Analyzing=9, Evaluating=3, Creating=1"
     */
    private String bloomAllocation;
}
