package com.example.demo.modules.quiz.evaluation.application;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.StateResponse;
import com.example.demo.enums.ErrorCode;
import com.example.demo.exception.HandleException;
import com.example.demo.modules.quiz.analytics.application.QuizResponseBuilder;
import com.example.demo.modules.quiz.api.dto.QuizSubmissionRequest;
import com.example.demo.modules.quiz.api.dto.QuizSubmissionResponse;
import com.example.demo.modules.quiz.archive.infrastructure.port.ArchivePort;
import com.example.demo.modules.quiz.shared.domain.model.UserAnswer;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.ArchivedQuestionMongoEntity;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.QuestionBankMongoEntity;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.UserResourceMongoEntity;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.repository.QuestionBankRepository;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.repository.UserResourceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Use case for submitting quiz answers, calculating scores, and updating IRT parameters.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SubmitQuizUseCase {

    private final UserResourceRepository userResourceRepository;
    private final QuestionBankRepository questionBankRepository;
    private final IRTCalculator irtCalculator;
    private final ArchivePort archivePort;
    private final MongoTemplate mongoTemplate;
    private final MessageSource messageSource;
    private final QuizResponseBuilder responseBuilder;

    @Transactional
    public StateResponse<Object> execute(QuizSubmissionRequest request, String username) {
        log.info("Processing quiz submission for user: {}, topic: {}", username, request.getTopic());

        // 1. Kiểm tra xem bài tập đã được nộp chưa (idempotency)
        ArchivedQuestionMongoEntity archive = archivePort.findById(request.getArchivedQuestionId())
                .orElseThrow(() -> new HandleException(ErrorCode.RESOURCE_NOT_FOUND));
        
        if (archive.isEvaluated()) {
            throw new HandleException(ErrorCode.EVALUATED_QUESTIONS);
        }

        // 2. Lấy hoặc tạo mới thông tin năng lực người dùng
        UserResourceMongoEntity userResource = userResourceRepository
                .findByUserNameAndTopic(username, request.getTopic())
                .orElseGet(() -> createNewUserResource(username, request.getTopic()));

        List<UserAnswer> answers = request.getAnswers();
        int totalQuestions = answers.size();
        int correctAnswers = (int) answers.stream().filter(UserAnswer::isTrue).count();
        double scorePercentage = (correctAnswers * 100.0) / totalQuestions;

        // 3. Tính toán Theta mới qua IRT-MAP
        // Note: Cần cẩn thận với kiểu dữ liệu history trong UserResource
        double[] irtResults = irtCalculator.reviewAnswer(
                answers,
                userResource.getTheta(),
                userResource.getHistory());

        double newTheta = irtResults[0];
        double newDifficulty = (irtResults[1] + irtResults[2]) / 2;

        userResource.setTheta(newTheta);
        userResource.setB(newDifficulty);

        // 4. Lưu lịch sử làm bài (giới hạn 200 bản ghi để tránh vượt quá 16MB document)
        List<UserAnswer> history = userResource.getHistory();
        history.addAll(answers);
        if (history.size() > 200) {
            userResource.setHistory(new java.util.ArrayList<>(history.subList(history.size() - 200, history.size())));
        }

        userResourceRepository.save(userResource);

        // 5. Hiệu chỉnh độ khó cho Question Bank
        recalibrateQuestionBank(answers, newTheta);

        // 6. Xây dựng kết quả trả về
        QuizSubmissionResponse response = QuizSubmissionResponse.builder()
                .totalQuestions(totalQuestions)
                .correctAnswers(correctAnswers)
                .scorePercentage(scorePercentage)
                .newTheta(newTheta)
                .newDifficulty(newDifficulty)
                .feedback(generateFeedback(scorePercentage))
                .build();

        return responseBuilder.buildSuccessResponse(response);
    }

    private void recalibrateQuestionBank(List<UserAnswer> answers, double userTheta) {
        List<String> bankIds = answers.stream()
                .map(UserAnswer::getBankId)
                .filter(Objects::nonNull)
                .toList();

        if (bankIds.isEmpty()) {
			return;
		}

        List<QuestionBankMongoEntity> bankedQuestions = questionBankRepository.findAllById(bankIds);
        Map<String, QuestionBankMongoEntity> questionMap = bankedQuestions.stream()
                .collect(Collectors.toMap(QuestionBankMongoEntity::getId, q -> q));

        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, "question_bank");
        boolean hasOps = false;

        for (UserAnswer ans : answers) {
            if (ans.getBankId() != null && questionMap.containsKey(ans.getBankId())) {
                QuestionBankMongoEntity bankedQ = questionMap.get(ans.getBankId());
                double calibratedB = irtCalculator.recalibrateItemDifficulty(
                        bankedQ.getDifficulty(),
                        userTheta,
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
        }
    }

    private UserResourceMongoEntity createNewUserResource(String username, String topic) {
        return UserResourceMongoEntity.builder()
                .userName(username)
                .topic(topic)
                .theta(0.0)
                .b(0.0)
                .sessionSize(15) // Default to 15 if created during submission (unlikely but safe)
                .history(new java.util.ArrayList<>())
                .contentIds(new java.util.ArrayList<>())
                .build();
    }

    private String generateFeedback(double scorePercentage) {
        java.util.Locale locale = LocaleContextHolder.getLocale();
        if (scorePercentage >= 90) {
			return messageSource.getMessage("feedback.excellent", null, locale);
		}
        if (scorePercentage >= 70) {
			return messageSource.getMessage("feedback.good", null, locale);
		}
        if (scorePercentage >= 50) {
			return messageSource.getMessage("feedback.fair", null, locale);
		}
        return messageSource.getMessage("feedback.poor", null, locale);
    }
    
}
