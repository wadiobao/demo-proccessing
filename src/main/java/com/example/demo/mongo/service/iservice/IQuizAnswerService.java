package com.example.demo.mongo.service.iservice;

import com.example.demo.dto.StateResponse;
import com.example.demo.mongo.dto.quiz.QuizSubmissionRequest;

/**
 * Interface for quiz answer submission and evaluation.
 */
public interface IQuizAnswerService {

    /**
     * Processes user's quiz answers and updates their learning profile.
     *
     * @param request  Quiz submission with answers
     * @param username Authenticated user's username
     * @return StateResponse containing submission results and updated IRT
     *         parameters
     * @throws Exception if processing fails
     */
    StateResponse<Object> submitQuizAnswers(QuizSubmissionRequest request, String username) throws Exception;

    /**
     * Retrieves user's learning statistics for a specific topic.
     *
     * @param username Username
     * @param topic    Topic/subject
     * @return StateResponse containing user statistics
     */
    StateResponse<Object> getUserStats(String username, String topic);

    /**
     * Retrieves global overview statistics for the radar chart and top-level
     * dashboard.
     *
     * @param username Authenticated user's username
     * @return StateResponse containing overview statistics
     *         (UserOverviewStatsResponse)
     */
    StateResponse<Object> getOverviewStats(String username);
}
