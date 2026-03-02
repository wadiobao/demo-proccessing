package com.example.demo.mongo.service.quiz;

import org.springframework.stereotype.Component;

import com.example.demo.mongo.dto.question.FileGenerateResponse;
import com.example.demo.mongo.entity.ArchivedQuestion;
import com.example.demo.mongo.entity.Content;
import com.example.demo.mongo.entity.QuestionBank;
import com.example.demo.mongo.repository.QuestionBankRepository;
import com.example.demo.mongo.service.ArchivedQuestionService;
import com.example.demo.mongo.service.iservice.IContentService;
import com.example.demo.mongo.service.iservice.IUserResourceService;
import com.example.demo.utils.GeneralUtils;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 * Coordinator for transactional persistence of quiz-related entities.
 * 
 * <p>
 * Quản lý việc lưu trữ dữ liệu theo thứ tự ưu tiên: Nội dung (Content) ->
 * Tài nguyên người dùng (UserResource) -> Kho lưu trữ câu hỏi
 * (ArchivedQuestion)
 * và cập nhật Ngân hàng câu hỏi.
 *
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class QuizPersistenceManager {

    IContentService contentService;
    IUserResourceService userResourceService;
    ArchivedQuestionService archivedQuestionService;
    QuestionBankRepository questionBankRepository;
    GeneralUtils generalUtils;

    /**
     * Persists all generated quiz data for authenticated users within a
     * transaction.
     * 
     * @param response   generated quiz content / nội dung bài tập đã tạo
     * @param username   author identification / tên người dùng thực hiện
     * @param filename   source file identifier / tên file nguồn
     * @param pdfContent source text / nội dung văn bản gốc
     * @param content    semantic metadata / thông tin chủ đề liên quan
     * @return enriched response with persistence IDs / phản hồi kèm theo mã định
     *         danh lưu trữ
     * @throws Exception if any step of the sequence fails / lỗi trong chuỗi tiến
     *                   trình lưu trữ
     */
    @Transactional
    public FileGenerateResponse persistQuizData(FileGenerateResponse response, String username, String filename,
            String pdfContent, Content content)
            throws Exception {

        log.info("Starting quiz data persistence for user: {}, file: {}", username, filename);

        // Step 1: Save Content only if it's new (doesn't have an ID yet)
        if (content.getId() == null) {
            content = contentService.save(pdfContent, username);
            log.info("New content saved with ID: {}", content.getId());
        } else {
            log.info("Using existing content/idempotent record: {}", content.getId());
        }

        final String contentId = content.getId();

        // Step 2: Save UserResource (linked to Content via topic)
        userResourceService.save(filename, pdfContent, username, content);
        log.debug("UserResource updated for topic: {}", content.getTopic());

        // Step 3: Save ArchivedQuestion (linked to Content via resourceId)
        ArchivedQuestion archivedQuestion = ArchivedQuestion.builder()
                .author(username)
                .questions(response.getQuestions())
                .pdfBase64(response.getPdfBase64())
                .wordBase64(response.getWordBase64())
                .title(filename)
                .resourceId(contentId)
                .build();

        archivedQuestionService.save(archivedQuestion);

        // Step 4: Populate Question Bank (New Feature - Phase 1)
        if (response.getQuestions() != null) {
            for (com.example.demo.mongo.dto.question.Question q : response.getQuestions()) {
                String qHash = generalUtils.sha256(q.getQuestion());
                java.util.Optional<com.example.demo.mongo.entity.QuestionBank> existingBanked = questionBankRepository
                        .findByContentIdAndQuestionHash(contentId, qHash);

                if (existingBanked.isEmpty()) {
                    QuestionBank bankedQ = QuestionBank.builder()
                            .contentId(contentId)
                            .questionHash(qHash)
                            .questionData(q)
                            .difficulty(q.getDifficulty()) // Use AI predicted difficulty as starting point
                            .build();
                    bankedQ = questionBankRepository.save(bankedQ);
                    q.setBankId(bankedQ.getId()); // Injected ID for future IRT calibration
                } else {
                    q.setBankId(existingBanked.get().getId());
                }
            }
            log.info("Question Bank processed for content: {}", contentId);
        }

        log.info("Quiz data persistence completed successfully for user: {}", username);

        FileGenerateResponse fileGenerateResponse = response;
        fileGenerateResponse.setTopic(content.getTopic());
        fileGenerateResponse.setArchivedQuestionId(archivedQuestion.getId());

        return fileGenerateResponse;
    }
}
