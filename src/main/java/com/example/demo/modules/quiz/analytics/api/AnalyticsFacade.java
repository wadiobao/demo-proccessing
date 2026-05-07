package com.example.demo.modules.quiz.analytics.api;

import com.example.demo.dto.StateResponse;

/**
 * Facade for Quiz and User Analytics.
 */
public interface AnalyticsFacade {
    /**
     * Retrieves learning stats for a specific topic.
     */
    StateResponse<Object> getUserStats(String username, String topic);

    /**
     * Retrieves global overview of user learning progress.
     */
    StateResponse<Object> getOverviewStats(String username);
}
