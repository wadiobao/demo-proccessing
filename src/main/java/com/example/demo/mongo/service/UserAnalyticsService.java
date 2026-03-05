package com.example.demo.mongo.service;

import java.util.List;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import org.springframework.stereotype.Service;

import com.example.demo.dto.StateResponse;
import com.example.demo.mongo.dto.question.UserAnswer;
import com.example.demo.mongo.dto.user.UserOverviewStatsResponse;
import com.example.demo.mongo.dto.user.UserOverviewStatsResponse.TopicMastery;
import com.example.demo.mongo.dto.user.UserStatsResponse;
import com.example.demo.mongo.entity.UserResource;
import com.example.demo.mongo.repository.UserResourceRepository;
import com.example.demo.mongo.service.iservice.IUserAnalyticsService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of UserAnalyticsService for tracking and reporting learning
 * progress.
 * 
 * <p>
 * Phân tích lịch sử làm bài, tính toán độ thành thạo theo Bloom Taxonomy
 * và tổng hợp dữ liệu cho biểu đồ Radar Dashboard.
 *
 * @since 1.2
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserAnalyticsService implements IUserAnalyticsService {

    UserResourceRepository userResourceRepository;
    MessageSource messageSource;

    @Override
    public StateResponse<Object> getUserStats(String username, String topic) {
        log.info("Retrieving stats for user: {}, topic: {}", username, topic);

        UserResource userResource = userResourceRepository
                .findByUserNameAndTopic(username, topic)
                .orElse(null);

        if (userResource == null) {
            log.warn("No stats found for user: {}, topic: {}", username, topic);
            return StateResponse.builder()
                    .message(messageSource.getMessage("quiz.stats.notfound", null, LocaleContextHolder.getLocale()))
                    .build();
        }

        // Calculate statistics
        List<UserAnswer> history = userResource.getHistory();
        int totalAnswered = history.size();
        int correctAnswers = (int) history.stream().filter(UserAnswer::isTrue).count();
        double accuracy = totalAnswered > 0 ? (correctAnswers * 100.0) / totalAnswered : 0.0;

        // cap history to last 20 answers for UI presentation
        List<UserAnswer> recentHistory = history.size() > 20
                ? history.subList(history.size() - 20, history.size())
                : history;

        // aggregate per Bloom level
        java.util.Map<String, Double> bloomStats = new java.util.HashMap<>();
        if (totalAnswered > 0) {
            java.util.Map<String, int[]> bloomCounts = new java.util.HashMap<>();
            for (UserAnswer answer : history) {
                String bloomLevel = answer.getBloomLevel() != null ? answer.getBloomLevel() : "Unknown";
                bloomCounts.putIfAbsent(bloomLevel, new int[] { 0, 0 }); // [total, correct]
                bloomCounts.get(bloomLevel)[0]++;
                if (answer.isTrue()) {
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
                .totalQuizzes(userResource.getContentIds().size())
                .totalQuestionsAnswered(totalAnswered)
                .accuracyPercentage(accuracy)
                .recentHistory(recentHistory)
                .bloomStats(bloomStats)
                .build();

        return StateResponse.builder()
                .result(stats)
                .message(messageSource.getMessage("quiz.stats.success", null, LocaleContextHolder.getLocale()))
                .build();
    }

    @Override
    public StateResponse<Object> getOverviewStats(String username) {
        log.info("Retrieving overview stats for user: {}", username);

        List<UserResource> userResources = userResourceRepository.findAllByUserName(username);

        if (userResources == null || userResources.isEmpty()) {
            log.warn("No resources found for user: {}", username);
            return StateResponse.builder()
                    .message(messageSource.getMessage("quiz.overview.empty", null, LocaleContextHolder.getLocale()))
                    .result(UserOverviewStatsResponse.builder()
                            .username(username)
                            .totalTopicsMastered(0)
                            .overallSkillLevel(0.0)
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

        for (UserResource resource : userResources) {
            List<UserAnswer> history = resource.getHistory();
            if (history != null && !history.isEmpty()) {
                totalQuestionsAnswered += history.size();
                totalCorrectAnswers += (int) history.stream().filter(UserAnswer::isTrue).count();
            }

            sumTheta += resource.getTheta();

            double masteryScale = Math.max(0, Math.min(100, ((resource.getTheta() + 3.0) / 6.0) * 100.0));
            masteryScale = Math.round(masteryScale * 10.0) / 10.0;

            radarChartData.add(TopicMastery.builder()
                    .topic(resource.getTopic())
                    .masteryLevel(masteryScale)
                    .build());
        }

        double overallAccuracy = totalQuestionsAnswered > 0
                ? ((double) totalCorrectAnswers * 100.0) / totalQuestionsAnswered
                : 0.0;
        overallAccuracy = Math.round(overallAccuracy * 10.0) / 10.0;

        double averageTheta = sumTheta / totalTopicsCount;
        double overallSkillLevel = Math.max(0, Math.min(100, ((averageTheta + 3.0) / 6.0) * 100.0));
        overallSkillLevel = Math.round(overallSkillLevel * 10.0) / 10.0;

        UserOverviewStatsResponse overview = UserOverviewStatsResponse.builder()
                .username(username)
                .totalTopicsMastered(totalTopicsCount)
                .overallSkillLevel(overallSkillLevel)
                .overallAccuracyPercentage(overallAccuracy)
                .totalQuestionsAnswered(totalQuestionsAnswered)
                .radarChartData(radarChartData)
                .build();

        return StateResponse.builder()
                .result(overview)
                .message(messageSource.getMessage("quiz.overview.success", null, LocaleContextHolder.getLocale()))
                .build();
    }
}
