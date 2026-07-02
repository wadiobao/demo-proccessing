package com.example.demo.modules.quiz.generation.application.usecase;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.modules.document.metadata.api.DocumentMetadataFacade;
import com.example.demo.modules.document.metadata.domain.model.DocumentMetadata;
import com.example.demo.modules.quiz.evaluation.application.service.IRTCalculator;
import com.example.demo.modules.quiz.shared.domain.model.FileGenerateResponse;
import com.example.demo.modules.quiz.shared.domain.model.Question;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.ArchivedSessionMongoEntity;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.QuestionBankMongoEntity;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.UserResourceMongoEntity;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.repository.ArchivedQuestionRepository;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.repository.QuestionBankRepository;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.repository.UserResourceRepository;
import com.example.demo.utils.GeneralUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Use case for persisting generated quiz data for authenticated users.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PersistQuizUseCase {

    private final DocumentMetadataFacade documentMetadataFacade;
    private final UserResourceRepository userResourceRepository;
    private final ArchivedQuestionRepository archivedQuestionRepository;
    private final QuestionBankRepository questionBankRepository;
    private final GeneralUtils generalUtils;
    private final IRTCalculator irtCalculator;

    @Transactional
    public FileGenerateResponse execute(FileGenerateResponse response, String username, String filename,
            String pdfContent, boolean shouldUpdateContentIds) throws Exception {
        return execute(response, username, filename, pdfContent, shouldUpdateContentIds, null, "ADAPTIVE");
    }

    @Transactional
    public FileGenerateResponse execute(FileGenerateResponse response, String username, String filename,
            String pdfContent, boolean shouldUpdateContentIds, String explicitTopic) throws Exception {
        return execute(response, username, filename, pdfContent, shouldUpdateContentIds, explicitTopic, "ADAPTIVE");
    }

    @Transactional
    public FileGenerateResponse execute(FileGenerateResponse response, String username, String filename,
            String pdfContent, boolean shouldUpdateContentIds, String explicitTopic, String type) throws Exception {
        log.info("Starting quiz data persistence for user: {}, file: {}, explicit topic: {}, type: {}", username, filename,
                explicitTopic, type);

        String topic = explicitTopic;
        String contentId = null;

        if ("ADAPTIVE".equalsIgnoreCase(type)) {
            // Only create DocumentMetadata if it's a single file (shouldUpdateContentIds =
            // true)
            // OR if explicitTopic is null (fallback to detect topic)
            if (shouldUpdateContentIds || explicitTopic == null) {
                DocumentMetadata metadata = documentMetadataFacade.findOrCreateMetadata(pdfContent, username, filename,
                        explicitTopic);
                contentId = metadata.getId();
                topic = metadata.getTopic();
            }

            // 2. Cập nhật UserResource (Lịch sử học tập theo Topic)
            updateUserResource(username, topic, contentId, shouldUpdateContentIds);
        }

        // 3. Lưu bản lưu trữ quiz (ArchivedQuestion)
        if ("PUBLIC".equalsIgnoreCase(type)) {
            ArchivedSessionMongoEntity archived = ArchivedSessionMongoEntity.builder()
                    .author(username)
                    .questions(response.getQuestions())
                    .pdfBase64(response.getPdfBase64())
                    .wordBase64(response.getWordBase64())
                    .excelBase64(response.getExcelBase64())
                    .title(filename)
                    .resourceId(contentId)
                    .build();

            archived = archivedQuestionRepository.save(archived);
            response.setArchivedQuestionId(archived.getId());
        }

        // 4. Cập nhật Question Bank (Ngân hàng câu hỏi dùng chung)
        processQuestionBank(response.getQuestions(), contentId);

        // 5. Cập nhật response
        response.setTopic(topic);

        log.info("Quiz data persistence completed for user: {}", username);
        return response;
    }

    private void updateUserResource(String username, String topic, String contentId, boolean shouldUpdateContentIds) {
        UserResourceMongoEntity u = userResourceRepository.findByUserNameAndTopic(username, topic)
                .orElseGet(() -> UserResourceMongoEntity.builder()
                        .userName(username)
                        .topic(topic)
                        .theta(0.0)
                        .b(0.0)
                        .history(new java.util.ArrayList<>())
                        .thetaHistory(new java.util.ArrayList<>())
                        .contentIds(new java.util.ArrayList<>())
                        .build());

        if (shouldUpdateContentIds && contentId != null && !u.getContentIds().contains(contentId)) {
            u.getContentIds().add(contentId);
        }

        userResourceRepository.save(u);
    }

    private void processQuestionBank(List<Question> questions, String contentId) {
        if (questions == null) {
            return;
        }

        for (Question q : questions) {
            String qHash = generalUtils.sha256(q.getQuestion());
            Optional<QuestionBankMongoEntity> existing = questionBankRepository
                    .findByContentIdAndQuestionHash(contentId, qHash);

            if (existing.isEmpty()) {
                QuestionBankMongoEntity bankedQ = QuestionBankMongoEntity.builder()
                        .contentId(contentId)
                        .questionHash(qHash)
                        .questionData(q)
                        .difficulty(q.getDifficulty())
                        .build();
                bankedQ = questionBankRepository.save(bankedQ);
                q.setBankId(bankedQ.getId());
            } else {
                q.setBankId(existing.get().getId());
            }
        }
    }
}
