package com.example.demo.modules.quiz.analytics.application;

import com.example.demo.modules.quiz.analytics.api.AnalyticsFacade;

import org.springframework.stereotype.Service;

import com.example.demo.dto.StateResponse;
import com.example.demo.dto.StateResponse;

import lombok.RequiredArgsConstructor;

/**
 * Implementation of AnalyticsFacade.
 */
@Service
@RequiredArgsConstructor
class AnalyticsFacadeImpl implements AnalyticsFacade {

    private final UserAnalyticsService userAnalyticsService;

    @Override
    public StateResponse<Object> getUserStats(String username, String topic) {
        return userAnalyticsService.getUserStats(username, topic);
    }

    @Override
    public StateResponse<Object> getOverviewStats(String username) {
        return userAnalyticsService.getOverviewStats(username);
    }
}
