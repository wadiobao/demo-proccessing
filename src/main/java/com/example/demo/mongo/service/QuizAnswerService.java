package com.example.demo.mongo.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.example.demo.dto.StateResponse;
import com.example.demo.enums.ErrorCode;
import com.example.demo.exception.HandleException;
import com.example.demo.mongo.dto.question.UserAnswer;
import com.example.demo.mongo.dto.quiz.QuizSubmissionRequest;
import com.example.demo.mongo.dto.quiz.QuizSubmissionResponse;
import com.example.demo.mongo.dto.user.UserOverviewStatsResponse;
import com.example.demo.mongo.dto.user.UserOverviewStatsResponse.TopicMastery;
import com.example.demo.mongo.dto.user.UserStatsResponse;
import com.example.demo.mongo.entity.QuestionBank;
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
 * Service for processing user quiz submissions and tracking learning progress.
 * 
 * <p>
 * Xử lý kết quả làm bài của người dùng, tự động cập nhật năng lực (Theta)
 * qua IRT, đánh giá lại độ khó câu hỏi và tạo báo cáo thống kê Topic Mastery.
 *
 * @since 1.0
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
        MongoTemplate mongoTemplate;

        /**
         * Processes a full quiz submission including IRT recalibration.
         * 
         * <p>
         * Thực hiện chấm điểm, cập nhật chỉ số Theta cho người dùng
         * và điều chỉnh tham số độ khó thực tế cho Ngân hàng câu hỏi.
         *
         * @param request  user answers and context / danh sách câu trả lời
         * @param username student identification / tên người dùng thực hiện
         * @return detailed evaluation response / kết quả đánh giá chi tiết
         * @throws Exception if submission is invalid or duplicate / lỗi nếu bài nộp
         *                   không hợp lệ hoặc đã đánh giá
         */
        @Override
        @Transactional
        public StateResponse<Object> submitQuizAnswers(QuizSubmissionRequest request, String username)
                        throws Exception {
                log.info("Processing quiz submission for user: {}, topic: {}", username, request.getTopic());

                if (iArchivedQuestionService.isEvaluated(request.getArchivedQuestionId())) {
                        log.info("Câu hỏi đã được đánh giá");
                        throw new HandleException(ErrorCode.EVALUATED_QUESTIONS);
                }

                // fallback: initialize a fresh profile when first dataset for this topic is
                // submitted
                UserResource userResource = userResourceRepository
                                .findByUserNameAndTopic(username, request.getTopic())
                                .orElseGet(() -> createNewUserResource(username, request.getTopic()));

                List<UserAnswer> answers = request.getAnswers();
                int totalQuestions = answers.size();
                int correctAnswers = (int) answers.stream().filter(UserAnswer::isTrue).count();
                double scorePercentage = (correctAnswers * 100.0) / totalQuestions;

                log.debug("Score: {}/{} ({}%)", correctAnswers, totalQuestions, scorePercentage);

                // IRT-MAP estimation: returns [newTheta, bMin, bMax] window from posterior
                double[] irtResults = irtCalculator.reviewAnswer(
                                answers,
                                userResource.getTheta(),
                                userResource.getHistory());

                double newTheta = irtResults[0];
                double bMin = irtResults[1];
                double bMax = irtResults[2];
                // midpoint of estimated difficulty interval for next quiz selection
                double newDifficulty = (bMin + bMax) / 2;

                userResource.setTheta(newTheta);
                userResource.setB(newDifficulty);
                userResource.getHistory().addAll(answers);

                userResourceRepository.save(userResource);

                // recalibrate banked questions using real-world performance signal
                // / Hiệu chỉnh độ khó của câu hỏi dựa trên phản hồi thực tế (hiệu quả hơn với
                // BulkOps)
                List<String> bankIds = answers.stream()
                                .map(UserAnswer::getBankId)
                                .filter(java.util.Objects::nonNull)
                                .collect(Collectors.toList());

                if (!bankIds.isEmpty()) {
                        List<QuestionBank> bankedQuestions = questionBankRepository.findAllById(bankIds);
                        java.util.Map<String, QuestionBank> questionMap = bankedQuestions.stream()
                                        .collect(Collectors.toMap(QuestionBank::getId, q -> q));

                        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED,
                                        QuestionBank.class);
                        boolean hasOps = false;

                        for (UserAnswer ans : answers) {
                                if (ans.getBankId() != null && questionMap.containsKey(ans.getBankId())) {
                                        QuestionBank bankedQ = questionMap.get(ans.getBankId());
                                        double calibratedB = irtCalculator.recalibrateItemDifficulty(
                                                        bankedQ.getDifficulty(),
                                                        userResource.getTheta(),
                                                        ans.isTrue(),
                                                        0.1);

                                        Query query = new Query(Criteria.where("_id").is(ans.getBankId()));
                                        Update update = new Update()
                                                        .inc("attempts", 1)
                                                        .set("difficulty", calibratedB);
                                        if (ans.isTrue()) {
                                                update.inc("correctCount", 1);
                                        }
                                        bulkOps.updateOne(query, update);
                                        hasOps = true;
                                }
                        }
                        if (hasOps) {
                                bulkOps.execute();
                                log.info("Bulk updated {} questions in QuestionBank", bankIds.size());
                        }
                }

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

                // cap history to last 20 answers: older responses have low IRT signal value
                // / giới hạn lịch sử 20 câu gần nhất: dữ liệu cũ ít ảnh hưởng đến ước tính IRT
                List<UserAnswer> recentHistory = history.size() > 20
                                ? history.subList(history.size() - 20, history.size())
                                : history;

                // aggregate per Bloom level to surface which cognitive tier the user struggles
                // with
                // / tổng hợp theo mức Bloom để lộ ra tầng nhận thức nào người dùng yếu nhất
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

        /**
         * Retrieves aggregated learning statistics for all topics of the user.
         * 
         * <p>
         * Sử dụng chỉ mục (index) trên trường {@code userName} để tránh
         * quét toàn bộ collection. Tổng hợp dữ liệu Theta và độ chính xác cho
         * biểu đồ radar (Radar Chart) và Dashboard tổng quan.
         *
         * @param username student identification / tên người dùng thực hiện
         * @return aggregated overview statistics / thống kê tổng hợp toàn phần
         */
        @Override
        public StateResponse<Object> getOverviewStats(String username) {
                log.info("Retrieving overview stats for user: {}", username);

                // P0 FIX: Use indexed query instead of findAll() full collection scan
                List<UserResource> userResources = userResourceRepository.findAllByUserName(username);

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

                        // Convert theta to a 0-100 mastery scale (theta range: -3.0 to +3.0)
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
                                .message("Lấy tổng quan học tập thành công")
                                .build();
        }
}
