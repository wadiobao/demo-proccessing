package com.example.demo.mongo.dto.user;

import java.util.List;

import com.example.demo.mongo.dto.question.UserAnswer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for user learning statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsResponse {

    /**
     * Username
     */
    private String username;

    /**
     * Topic/subject
     */
    private String topic;

    /**
     * Current ability level (IRT theta parameter)
     */
    private double theta;

    /**
     * Current difficulty level (IRT b parameter)
     */
    private double difficulty;

    /**
     * Total number of quizzes completed
     */
    private int totalQuizzes;

    /**
     * Total questions answered
     */
    private int totalQuestionsAnswered;

    /**
     * Overall accuracy percentage
     */
    private double accuracyPercentage;

    /**
     * Answer history (recent answers)
     */
    private List<UserAnswer> recentHistory;
}
