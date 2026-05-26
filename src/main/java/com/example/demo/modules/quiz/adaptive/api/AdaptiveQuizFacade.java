package com.example.demo.modules.quiz.adaptive.api;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.quiz.adaptive.api.request.QuizSubmissionRequest;
import com.example.demo.modules.quiz.shared.domain.model.QuizConfig;

/**
 * Facade for Adaptive Learning and Private Quiz operations.
 */
public interface AdaptiveQuizFacade {
    /**
     * Coordinate private quiz generation (authenticated users).
     */
    StateResponse<Object> generatePrivateQuiz(List<MultipartFile> files, QuizConfig config, String username) throws Exception;

    /**
     * Generate review quiz for an existing topic (uses all files in topic).
     */
    StateResponse<Object> generateReviewQuiz(String topicId, QuizConfig config, String username, String requestId) throws Exception;

    /**
     * Creates or updates a topic resource with initial documents and settings.
     */
    StateResponse<Object> createTopic(String topic, List<MultipartFile> files, int sessionSize, String username) throws Exception;

    /**
     * Updates an existing topic resource name.
     */
    StateResponse<Object> updateTopic(String id, String newTopicName, String username);

    /**
     * Deletes an existing topic resource.
     */
    StateResponse<Object> deleteTopic(String id, String username);

    /**
     * Submit quiz answers for evaluation and IRT update.
     */
    StateResponse<Object> submitQuiz(QuizSubmissionRequest request, String username);

    /**
     * Retrieve learning stats for a specific topic.
     */
    StateResponse<Object> getUserStats(String username, String topic);

    /**
     * Retrieve global overview of user learning progress.
     */
    StateResponse<Object> getOverviewStats(String username);

    /**
     * Add a file to a user's topic by id.
     */
    StateResponse<Object> addFileToTopic(MultipartFile file, String id, String username) throws Exception;

    /**
     * Get the list of files in a user's topic by id.
     */
    StateResponse<Object> getTopicFiles(String id, String username);
    
    /**
     * Delete a file in a user's topic by id.
     */
    StateResponse<Object> deleteTopicFiles(String topicId, String fileId, String username);

    /**
     * Get information of all topics for a user.
     */
    StateResponse<Object> getAllTopicsInfo(String username);

    /**
     * Get the theta score history of a topic by id.
     */
    StateResponse<Object> getTopicScoreHistory(String id, String username);

    /**
     * Get the unified topic overview (IRT stats + ELO + theta history + files) in a single call.
     */
    StateResponse<Object> getTopicOverview(String id, String username);
}
