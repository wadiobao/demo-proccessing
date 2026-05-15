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

    /** Feedback message */
    private String feedback;

    /** Total ELO score across all Bloom levels (0–1200) / Tổng ELO qua các cấp Bloom. */
    private int oldElo;
    private int newElo;
    private int deltaElo;

    /** Human-readable label of the current mastery level / Nhãn cấp độ thành thạo hiện tại. */
    private String oldMasteryLabel;
    private String newMasteryLabel;

    /** leveledUps>0 if the user advanced to a higher mastery level this session / Người dùng vừa lên cấp trong phiên này. */
    private int leveledUp;
}
