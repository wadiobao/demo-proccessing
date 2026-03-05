package com.example.demo.mongo.service.iservice;

import com.example.demo.dto.StateResponse;

/**
 * Service interface for user learning analytics and statistics.
 * 
 * <p>
 * Định nghĩa các phương thức trích xuất dữ liệu thống kê, biểu đồ năng lực
 * và dashboard tổng quan cho người dùng.
 *
 * @since 1.2
 */
public interface IUserAnalyticsService {

    /**
     * Retrieves user's learning statistics for a specific topic.
     *
     * @param username student id
     * @param topic    subject area
     * @return StateResponse with UserStatsResponse
     */
    StateResponse<Object> getUserStats(String username, String topic);

    /**
     * Retrieves aggregated learning statistics for all topics of the user.
     *
     * @param username student id
     * @return StateResponse with UserOverviewStatsResponse
     */
    StateResponse<Object> getOverviewStats(String username);
}
