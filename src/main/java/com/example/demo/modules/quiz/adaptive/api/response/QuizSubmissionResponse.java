package com.example.demo.modules.quiz.adaptive.api.response;

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

    /** Feedback message */
    private String feedback;

    /** Total ELO score across all Bloom levels (0–1200) / Tổng ELO qua các cấp Bloom. */
    private int elo;

    /** ELO points remaining to reach the next mastery level / ELO còn thiếu để lên cấp tiếp theo. */
    private int eloToNextLevel;

    /** Human-readable label of the current mastery level / Nhãn cấp độ thành thạo hiện tại. */
    private String masteryLabel;

    /** True if the user advanced to a higher mastery level this session / Người dùng vừa lên cấp trong phiên này. */
    private boolean leveledUp;
}
