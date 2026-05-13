package com.example.demo.modules.quiz.evaluation.application.usecase;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.StateResponse;
import com.example.demo.enums.ErrorCode;
import com.example.demo.exception.HandleException;
import com.example.demo.modules.quiz.adaptive.api.request.QuizSubmissionRequest;
import com.example.demo.modules.quiz.adaptive.api.response.QuizSubmissionResponse;
import com.example.demo.modules.quiz.archive.infrastructure.port.ArchivePort;
import com.example.demo.modules.quiz.evaluation.application.service.IRTCalculator;
import com.example.demo.modules.quiz.shared.application.QuizResponseBuilder;
import com.example.demo.modules.quiz.shared.domain.model.ThetaSnapshot;
import com.example.demo.modules.quiz.shared.domain.model.UserAnswer;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.ArchivedQuestionMongoEntity;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.QuestionBankMongoEntity;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.UserResourceMongoEntity;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.repository.QuestionBankRepository;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.repository.UserResourceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Use case for submitting quiz answers, calculating scores, and updating IRT
 * parameters.
 */
@Service
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

        // 2. Lấy thông tin năng lực người dùng (phải tồn tại vì UserResource được tạo
        // khi generate quiz)
        UserResourceMongoEntity userResource = userResourceRepository
                .findByUserNameAndTopic(username, request.getTopic())
                .orElseThrow(() -> new HandleException(ErrorCode.RESOURCE_NOT_FOUND));

        List<UserAnswer> answers = request.getAnswers();
        if (answers == null) {
            answers = new ArrayList<>();
        }
        int totalQuestions = archive.getQuestions().size();
        int correctAnswers = (int) answers.stream().filter(UserAnswer::isCorrect).count();
        double scorePercentage = totalQuestions > 0 ? (correctAnswers * 100.0) / totalQuestions : 0.0;

        // 3. Tính toán Theta mới qua IRT-MAP
        // Defensive copy: tránh side-effect ẩn khi reviewAnswer mutate list gốc
        List<UserAnswer> historyCopy = new ArrayList<>(userResource.getHistory());

        double[] irtResults = irtCalculator.reviewAnswer(
                answers,
                userResource.getTheta(),
                historyCopy);

        // Gán lại bản copy (đã được thêm câu mới) vào entity
        userResource.setHistory(historyCopy);

        double newTheta = irtResults[0];
        double newDifficulty = (irtResults[1] + irtResults[2]) / 2;
        int mastery = irtCalculator.calculateMasteryLevel(newTheta);

        // [EN] Compute ELO metrics from the new theta value
        // [VI] Tính toán các chỉ số ELO từ điểm theta mới
        int elo = irtCalculator.thetaToElo(newTheta);
        int eloToNext = irtCalculator.eloToNextLevel(newTheta);
        String masteryLabel = irtCalculator.getMasteryLabel(mastery);

        userResource.setTheta(newTheta);
        userResource.setB(newDifficulty);
        userResource.setMastery(mastery);
        userResource.setElo(elo);

        // record this session's score for historical trend tracking, capped at 100
        // entries
        ThetaSnapshot snapshot = ThetaSnapshot.builder()
                .theta(newTheta)
                .accuracy(scorePercentage)
                .recordedAt(LocalDateTime.now())
                .build();

        List<ThetaSnapshot> thetaHistory = userResource.getThetaHistory();
        if (thetaHistory == null) {
            thetaHistory = new java.util.ArrayList<>();
            userResource.setThetaHistory(thetaHistory);
        }

        thetaHistory.add(snapshot);
        if (thetaHistory.size() > 100) {
            userResource.setThetaHistory(
                    new java.util.ArrayList<>(thetaHistory.subList(thetaHistory.size() - 100, thetaHistory.size())));
        }

        // 4. Lưu lịch sử làm bài (giới hạn 200 bản ghi để tránh vượt quá 16MB document)
        // history đã được gán lại từ historyCopy ở bước 3
        List<UserAnswer> history = userResource.getHistory();
        if (history.size() > 200) {
            userResource.setHistory(new ArrayList<>(history.subList(history.size() - 200, history.size())));
        }

        userResourceRepository.save(userResource);

        // Mark archive as evaluated and save
        archive.setEvaluated(true);
        archivePort.save(archive);

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
                .elo(elo)
                .eloToNextLevel(eloToNext)
                .masteryLabel(masteryLabel)
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
                        ans.isCorrect(),
                        0.1);

                Query query = new Query(Criteria.where("_id").is(ans.getBankId()));
                Update update = new Update()
                        .inc("attempts", 1)
                        .set("difficulty", calibratedB);
                if (ans.isCorrect()) {
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
