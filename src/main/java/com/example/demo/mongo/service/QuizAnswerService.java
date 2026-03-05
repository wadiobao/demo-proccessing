package com.example.demo.mongo.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.OptimisticLockingFailureException;
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
import com.example.demo.mongo.entity.QuestionBank;
import com.example.demo.mongo.entity.UserResource;
import com.example.demo.mongo.repository.QuestionBankRepository;
import com.example.demo.mongo.repository.UserResourceRepository;
import com.example.demo.mongo.service.iservice.IArchivedQuestionService;
import com.example.demo.mongo.service.iservice.IQuizAnswerService;
import com.example.demo.utils.IRTCalculator;

import org.springframework.transaction.annotation.Transactional;
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
        MessageSource messageSource;

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
        public StateResponse<Object> submitQuizAnswers(QuizSubmissionRequest request, String username)
                        throws Exception {
                int maxRetries = 3;
                int attempts = 0;

                while (attempts < maxRetries) {
                        try {
                                return executeSubmit(request, username);
                        } catch (OptimisticLockingFailureException e) {
                                attempts++;
                                log.warn("Optimistic locking failure for user: {}, attempt: {}/{}", username, attempts,
                                                maxRetries);
                                if (attempts >= maxRetries) {
                                        throw e;
                                }
                                // Small delay before retry
                                Thread.sleep(100 * attempts);
                        }
                }
                throw new Exception(messageSource.getMessage("error.concurrency_failure", null,
                                LocaleContextHolder.getLocale()));
        }

        /**
         * Core logic for quiz submission execution, supporting retries.
         */
        @Transactional
        private StateResponse<Object> executeSubmit(QuizSubmissionRequest request, String username) throws Exception {
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

                // P1 FIX: Cap history to last 200 answers to prevent MongoDB 16MB document
                // bloat.
                // IRT performance is preserved as older signal is already baked into Theta.
                List<UserAnswer> history = userResource.getHistory();
                history.addAll(answers);
                if (history.size() > 200) {
                        log.info("Capping history for user: {} (size: {} -> 200)", username, history.size());
                        userResource.setHistory(new java.util.ArrayList<>(
                                        history.subList(history.size() - 200, history.size())));
                } else {
                        userResource.setHistory(history);
                }

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

                return StateResponse.<Object>builder()
                                .result(response)
                                .message(messageSource.getMessage("quiz.submit.success", null,
                                                LocaleContextHolder.getLocale()))
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
                java.util.Locale locale = LocaleContextHolder.getLocale();
                if (scorePercentage >= 90) {
                        return messageSource.getMessage("feedback.excellent", null, locale);
                } else if (scorePercentage >= 70) {
                        return messageSource.getMessage("feedback.good", null, locale);
                } else if (scorePercentage >= 50) {
                        return messageSource.getMessage("feedback.fair", null, locale);
                } else {
                        return messageSource.getMessage("feedback.poor", null, locale);
                }
        }
}
