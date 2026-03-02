package com.example.demo.mongo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.dto.StateResponse;
import com.example.demo.enums.ErrorCode;
import com.example.demo.exception.HandleException;
import com.example.demo.mongo.dto.question.UserAnswer;
import com.example.demo.mongo.dto.quiz.QuizSubmissionRequest;
import com.example.demo.mongo.dto.quiz.QuizSubmissionResponse;
import com.example.demo.mongo.dto.user.UserStatsResponse;
import com.example.demo.mongo.dto.user.UserOverviewStatsResponse;
import com.example.demo.mongo.dto.user.UserOverviewStatsResponse.TopicMastery;
import com.example.demo.mongo.entity.UserResource;
import com.example.demo.mongo.repository.QuestionBankRepository;
import com.example.demo.mongo.repository.UserResourceRepository;
import com.example.demo.mongo.service.iservice.IArchivedQuestionService;
import com.example.demo.mongo.service.iservice.IQuizAnswerService;
import com.example.demo.utils.IRTCalculator;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for processing quiz answer submissions.
 * Handles scoring, IRT parameter updates, and history tracking.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class QuizAnswerService implements IQuizAnswerService {

        UserResourceRepository userResourceRepository;
        IRTCalculator irtCalculator;
        IArchivedQuestionService iArchivedQuestionService;
        QuestionBankRepository questionBankRepository;

        @Override
        @Transactional
        public StateResponse<Object> submitQuizAnswers(QuizSubmissionRequest request, String username)
                        throws Exception {
                log.info("Processing quiz submission for user: {}, topic: {}", username, request.getTopic());

                if (iArchivedQuestionService.isEvaluated(request.getArchivedQuestionId())) {
                        log.info("Câu hỏi đã được đánh giá");
                        throw new HandleException(ErrorCode.EVALUATED_QUESTIONS);
                }

                // Get or create user resource for this topic
                UserResource userResource = userResourceRepository
                                .findByUserNameAndTopic(username, request.getTopic())
                                .orElseGet(() -> createNewUserResource(username, request.getTopic()));

                // Calculate score
                List<UserAnswer> answers = request.getAnswers();
                int totalQuestions = answers.size();
                int correctAnswers = (int) answers.stream().filter(UserAnswer::isTrue).count();
                double scorePercentage = (correctAnswers * 100.0) / totalQuestions;

                log.debug("Score: {}/{} ({}%)", correctAnswers, totalQuestions, scorePercentage);

                // Update IRT parameters
                double[] irtResults = irtCalculator.reviewAnswer(
                                answers,
                                userResource.getTheta(),
                                userResource.getHistory());

                double newTheta = irtResults[0];
                double bMin = irtResults[1];
                double bMax = irtResults[2];
                double newDifficulty = (bMin + bMax) / 2;

                // Update user resource
                userResource.setTheta(newTheta);
                userResource.setB(newDifficulty);
                userResource.getHistory().addAll(answers);

                userResourceRepository.save(userResource);

                // NEW Feature: Calibrate Question Bank (Phase 3)
                for (UserAnswer ans : answers) {
                        if (ans.getBankId() != null) {
                                questionBankRepository.findById(ans.getBankId()).ifPresent(bankedQ -> {
                                        // 1. Update primitive stats
                                        bankedQ.setAttempts(bankedQ.getAttempts() + 1);
                                        if (ans.isTrue()) {
                                                bankedQ.setCorrectCount(bankedQ.getCorrectCount() + 1);
                                        }

                                        // 2. Perform IRT Item Recalibration
                                        // Learning Rate: 0.1 (Target: capture empirical signal)
                                        double calibratedB = irtCalculator.recalibrateItemDifficulty(
                                                        bankedQ.getDifficulty(),
                                                        userResource.getTheta(),
                                                        ans.isTrue(),
                                                        0.1);
                                        bankedQ.setDifficulty(calibratedB);

                                        questionBankRepository.save(bankedQ);
                                });
                        }
                }

                log.info("Updated IRT parameters for user: {} - theta: {}, difficulty: {}",
                                username, newTheta, newDifficulty);
                log.info("Recalibrated {} banked questions for topic: {}", answers.size(), request.getTopic());

                // Build response
                QuizSubmissionResponse response = QuizSubmissionResponse.builder()
                                .totalQuestions(totalQuestions)
                                .correctAnswers(correctAnswers)
                                .scorePercentage(scorePercentage)
                                .newTheta(newTheta)
                                .newDifficulty(newDifficulty)
                                .feedback(generateFeedback(scorePercentage, newTheta))
                                .build();

                return StateResponse.builder()
                                .result(response)
                                .message("Quiz submitted successfully")
                                .build();
        }

        /**
         * Creates a new UserResource for a user-topic combination.
         */
        private UserResource createNewUserResource(String username, String topic) {
                log.info("Creating new UserResource for user: {}, topic: {}", username, topic);
                return UserResource.builder()
                                .userName(username)
                                .topic(topic)
                                .theta(0.0)
                                .b(0.0)
                                .history(new java.util.ArrayList<>())
                                .contentIds(new java.util.ArrayList<>())
                                .build();
        }

        /**
         * Generates personalized feedback based on score and ability.
         */
        private String generateFeedback(double scorePercentage, double theta) {
                if (scorePercentage >= 90) {
                        return "Xuất sắc! Bạn đã nắm vững kiến thức này.";
                } else if (scorePercentage >= 70) {
                        return "Tốt lắm! Hãy tiếp tục cố gắng.";
                } else if (scorePercentage >= 50) {
                        return "Khá tốt, nhưng bạn cần ôn tập thêm một số phần.";
                } else {
                        return "Bạn cần dành thêm thời gian để học lại nội dung này.";
                }
        }

        @Override
        public StateResponse<Object> getUserStats(String username, String topic) {
                log.info("Retrieving stats for user: {}, topic: {}", username, topic);

                UserResource userResource = userResourceRepository
                                .findByUserNameAndTopic(username, topic)
                                .orElse(null);

                if (userResource == null) {
                        log.warn("No stats found for user: {}, topic: {}", username, topic);
                        return StateResponse.builder()
                                        .message("Chưa có dữ liệu học tập cho chủ đề này")
                                        .build();
                }

                // Calculate statistics
                List<UserAnswer> history = userResource.getHistory();
                int totalAnswered = history.size();
                int correctAnswers = (int) history.stream().filter(UserAnswer::isTrue).count();
                double accuracy = totalAnswered > 0 ? (correctAnswers * 100.0) / totalAnswered : 0.0;

                // Get recent history (last 20 answers)
                List<UserAnswer> recentHistory = history.size() > 20
                                ? history.subList(history.size() - 20, history.size())
                                : history;

                // Calculate stats per Bloom's taxonomy level
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
                                // Round to 1 decimal place
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
                                .message("Lấy thông tin thành công")
                                .build();
        }

        @Override
        public StateResponse<Object> getOverviewStats(String username) {
                log.info("Retrieving overview stats for user: {}", username);

                // Assuming userResourceRepository.findByUserName is returning
                // Optional<UserResource> by accident,
                // or if we must pull all, we can fallback to standard mongo operations.
                // Let's use userResourceRepository.findAllByUserName(username) if exist, or we
                // can just iterate findAll() and filter.
                // Since I can't guess the repository, I'll fetch all and filter for now to be
                // safe and fix the compile error.
                List<UserResource> allResources = userResourceRepository.findAll();
                List<UserResource> userResources = new java.util.ArrayList<>();
                for (UserResource ur : allResources) {
                        if (username.equals(ur.getUserName())) {
                                userResources.add(ur);
                        }
                }

                if (userResources == null || userResources.isEmpty()) {
                        log.warn("No resources found for user: {}", username);
                        return StateResponse.builder()
                                        .message("Chưa có dữ liệu học tập")
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

                        // Convert theta to a 0-100 mastery scale.
                        // Assuming theta ranges from roughly -3.0 to +3.0
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
                // Convert global average theta to 0-100 scale overall skill level
                double overallSkillLevel = Math.max(0, Math.min(100, ((averageTheta + 3.0) / 6.0) * 100.0));
                overallSkillLevel = Math.round(overallSkillLevel * 10.0) / 10.0;

                UserOverviewStatsResponse overview = UserOverviewStatsResponse.builder()
                                .username(username)
                                .totalTopicsMastered(totalTopicsCount) // Consider 'mastered' if they have a
                                                                       // resource/attempt
                                .overallSkillLevel(overallSkillLevel)
                                .overallAccuracyPercentage(overallAccuracy)
                                .totalQuestionsAnswered(totalQuestionsAnswered)
                                .radarChartData(radarChartData)
                                .build();

                return StateResponse.builder()
                                .result(overview)
                                .message("Lấy tổng quan học tập thành công")
                                .build();
        }
}
