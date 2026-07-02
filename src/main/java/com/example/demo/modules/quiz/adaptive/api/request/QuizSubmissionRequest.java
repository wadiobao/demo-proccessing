package com.example.demo.modules.quiz.adaptive.api.request;

import java.util.List;

import com.example.demo.modules.quiz.shared.domain.model.UserAnswer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for submitting quiz answers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizSubmissionRequest {

    /**
     * Topic of the quiz (for IRT tracking)
     */
    private String topic;

    /**
     * List of user answers with question IDs and correctness
     */
    private List<UserAnswer> answers;

    /**
     * Unique ID of the quiz session request (for tracking review/adaptive quizzes)
     */
    private String requestId;
}
