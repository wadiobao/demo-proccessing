package com.example.demo.modules.quiz.analytics.application;

import java.util.List;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.quiz.adaptive.api.response.UserOverviewStatsResponse;
import com.example.demo.modules.quiz.adaptive.api.response.UserOverviewStatsResponse.TopicMastery;
import com.example.demo.modules.quiz.adaptive.api.response.UserStatsResponse;
import com.example.demo.modules.quiz.shared.domain.model.UserAnswer;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.UserResourceMongoEntity;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.repository.UserResourceRepository;
import com.example.demo.modules.quiz.evaluation.application.service.IRTCalculator;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for analyzing user learning progress and statistics.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
class UserAnalyticsService {

    UserResourceRepository userResourceRepository;
    MessageSource messageSource;
    IRTCalculator irtCalculator;

    public StateResponse<Object> getUserStats(String username, String topic) {
        log.info("Retrieving stats for user: {}, topic: {}", username, topic);

        UserResourceMongoEntity userResource = userResourceRepository
                .findByUserNameAndTopic(username, topic)
                .orElse(null);

        if (userResource == null) {
            log.warn("No stats found for user: {}, topic: {}", username, topic);
            return StateResponse.<Object>builder()
                    .message(messageSource.getMessage("quiz.stats.notfound", null, LocaleContextHolder.getLocale()))
                    .build();
        }

        List<UserAnswer> history = userResource.getHistory();
        int totalAnswered = history.size();
        int correctAnswers = (int) history.stream().filter(UserAnswer::isCorrect).count();
        double accuracy = totalAnswered > 0 ? (correctAnswers * 100.0) / totalAnswered : 0.0;

        List<UserAnswer> recentHistory = history.size() > 20
                ? history.subList(history.size() - 20, history.size())
                : history;

        java.util.Map<String, Double> bloomStats = new java.util.HashMap<>();
        if (totalAnswered > 0) {
            java.util.Map<String, int[]> bloomCounts = new java.util.HashMap<>();
            for (UserAnswer answer : history) {
                String bloomLevel = answer.getBloomLevel() != null ? answer.getBloomLevel() : "Unknown";
                bloomCounts.putIfAbsent(bloomLevel, new int[] { 0, 0 });
                bloomCounts.get(bloomLevel)[0]++;
                if (answer.isCorrect()) {
                    bloomCounts.get(bloomLevel)[1]++;
                }
            }

            for (java.util.Map.Entry<String, int[]> entry : bloomCounts.entrySet()) {
                double bloomAccuracy = (entry.getValue()[1] * 100.0) / entry.getValue()[0];
                bloomAccuracy = Math.round(bloomAccuracy * 10.0) / 10.0;
                bloomStats.put(entry.getKey(), bloomAccuracy);
            }
        }

        UserStatsResponse stats = UserStatsResponse.builder()
                .username(username)
                .topic(topic)
                .theta(userResource.getTheta())
                .difficulty(userResource.getB())
                .totalQuizzes(userResource.getContentIds() != null ? userResource.getContentIds().size() : 0)
                .totalQuestionsAnswered(totalAnswered)
                .accuracyPercentage(accuracy)
                .recentHistory(recentHistory) // No mapping needed
                .bloomStats(bloomStats)
                .build();

        return StateResponse.<Object>builder()
                .result(stats)
                .message(messageSource.getMessage("quiz.stats.success", null, LocaleContextHolder.getLocale()))
                .build();
    }

    
    public StateResponse<Object> getOverviewStats(String username) {
        log.info("Retrieving overview stats for user: {}", username);

        List<UserResourceMongoEntity> userResources = userResourceRepository.findAllByUserName(username);

        if (userResources == null || userResources.isEmpty()) {
            return StateResponse.<Object>builder()
                    .message(messageSource.getMessage("quiz.overview.empty", null, LocaleContextHolder.getLocale()))
                    .result(UserOverviewStatsResponse.builder()
                            .username(username)
                            .totalTopicsMastered(0)
                            .averageElo(0)
                            .overallAccuracyPercentage(0.0)
                            .totalQuestionsAnswered(0)
                            .radarChartData(new java.util.ArrayList<>())
                            .build())
                    .build();
        }

        int totalTopicsCount = userResources.size();
        int totalQuestionsAnswered = 0;
        int totalCorrectAnswers = 0;
        double sumTheta = 0.0;

        List<TopicMastery> radarChartData = new java.util.ArrayList<>();

        for (UserResourceMongoEntity resource : userResources) {
            List<UserAnswer> history = resource.getHistory();
            if (history != null && !history.isEmpty()) {
                totalQuestionsAnswered += history.size();
                totalCorrectAnswers += (int) history.stream().filter(UserAnswer::isCorrect).count();
            }

            sumTheta += resource.getTheta();

            int elo = resource.getElo();
            if (elo == 0) {
                elo = irtCalculator.thetaToElo(resource.getTheta());
            }

            int mastery = resource.getMastery() == 0
                    ? irtCalculator.calculateMasteryLevel(resource.getTheta())
                    : resource.getMastery();
            String masteryLabel = irtCalculator.getMasteryLabel(mastery);

            radarChartData.add(TopicMastery.builder()
                    .topic(resource.getTopic())
                    .elo(elo)
                    .masteryLabel(masteryLabel)
                    .build());
        }

        double overallAccuracy = totalQuestionsAnswered > 0
                ? (totalCorrectAnswers * 100.0) / totalQuestionsAnswered
                : 0.0;
        overallAccuracy = Math.round(overallAccuracy * 10.0) / 10.0;

        double averageTheta = sumTheta / totalTopicsCount;
        int averageElo = irtCalculator.thetaToElo(averageTheta);

        UserOverviewStatsResponse overview = UserOverviewStatsResponse.builder()
                .username(username)
                .totalTopicsMastered(totalTopicsCount)
                .averageElo(averageElo)
                .overallAccuracyPercentage(overallAccuracy)
                .totalQuestionsAnswered(totalQuestionsAnswered)
                .radarChartData(radarChartData)
                .build();

        return StateResponse.<Object>builder()
                .result(overview)
                .message(messageSource.getMessage("quiz.overview.success", null, LocaleContextHolder.getLocale()))
                .build();
    }
}
