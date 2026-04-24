package com.example.demo.modules.quiz.api.dto;

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
     * ID of the archived question set (optional, for tracking)
     */
    private String archivedQuestionId;

    /**
     * Topic of the quiz (for IRT tracking)
     */
    private String topic;

    /**
     * List of user answers with question IDs and correctness
     */
    private List<UserAnswer> answers;
}
