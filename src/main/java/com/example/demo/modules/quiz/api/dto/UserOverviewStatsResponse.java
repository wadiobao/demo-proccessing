package com.example.demo.modules.quiz.api.dto;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserOverviewStatsResponse {
    String username;
    int totalTopicsMastered;
    double overallSkillLevel;
    double overallAccuracyPercentage;
    int totalQuestionsAnswered;
    List<TopicMastery> radarChartData;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopicMastery {
        private String topic;
        private double masteryLevel; // Converted from theta (0-100)
    }
}
