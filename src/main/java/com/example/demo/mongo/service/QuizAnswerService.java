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
import com.example.demo.mongo.entity.UserResource;
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

                log.info("Updated IRT parameters for user: {} - theta: {}, difficulty: {}",
                                username, newTheta, newDifficulty);

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
}
