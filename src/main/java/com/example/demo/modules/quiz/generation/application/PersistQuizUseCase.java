package com.example.demo.modules.quiz.generation.application;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.modules.document.metadata.api.DocumentMetadataFacade;
import com.example.demo.modules.document.metadata.domain.model.DocumentMetadata;
import com.example.demo.modules.quiz.shared.domain.model.FileGenerateResponse;
import com.example.demo.modules.quiz.shared.domain.model.Question;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.ArchivedQuestionMongoEntity;
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
@Component
@RequiredArgsConstructor
@Slf4j
public class PersistQuizUseCase {

    private final DocumentMetadataFacade documentMetadataFacade;
    private final UserResourceRepository userResourceRepository;
    private final ArchivedQuestionRepository archivedQuestionRepository;
    private final QuestionBankRepository questionBankRepository;
    private final GeneralUtils generalUtils;

    @Transactional
    public FileGenerateResponse execute(FileGenerateResponse response, String username, String filename,
            String pdfContent) throws Exception {
        log.info("Starting quiz data persistence for user: {}, file: {}", username, filename);

        // 1. Đảm bảo Metadata tồn tại
        DocumentMetadata metadata = documentMetadataFacade.findOrCreateMetadata(pdfContent, username);
        final String contentId = metadata.getId();

        // 2. Cập nhật UserResource (Lịch sử học tập theo Topic)
        updateUserResource(username, metadata);

        // 3. Lưu bản lưu trữ quiz (ArchivedQuestion)
        ArchivedQuestionMongoEntity archived = ArchivedQuestionMongoEntity.builder()
                .author(username)
                .questions(response.getQuestions())
                .pdfBase64(response.getPdfBase64())
                .wordBase64(response.getWordBase64())
                .title(filename)
                .resourceId(contentId)
                .build();

        archived = archivedQuestionRepository.save(archived);

        // 4. Cập nhật Question Bank (Ngân hàng câu hỏi dùng chung)
        processQuestionBank(response.getQuestions(), contentId);

        // 5. Cập nhật response
        response.setTopic(metadata.getTopic());
        response.setArchivedQuestionId(archived.getId());

        log.info("Quiz data persistence completed for user: {}", username);
        return response;
    }

    private void updateUserResource(String username, DocumentMetadata metadata) {
        UserResourceMongoEntity u = userResourceRepository.findByUserNameAndTopic(username, metadata.getTopic())
                .orElseGet(() -> UserResourceMongoEntity.builder()
                        .userName(username)
                        .topic(metadata.getTopic())
                        .theta(0.0)
                        .b(0.0)
                        .history(new java.util.ArrayList<>())
                        .contentIds(new java.util.ArrayList<>())
                        .build());

        if (!u.getContentIds().contains(metadata.getId())) {
            u.getContentIds().add(metadata.getId());
        }

        userResourceRepository.save(u);
    }

    private void processQuestionBank(List<Question> questions, String contentId) {
        if (questions == null)
            return;

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
