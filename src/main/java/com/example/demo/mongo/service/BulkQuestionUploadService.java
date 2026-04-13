package com.example.demo.mongo.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.enums.VerificationStatus;
import com.example.demo.mongo.dto.question.Question;
import com.example.demo.mongo.entity.QuestionBank;
import com.example.demo.mongo.repository.QuestionBankRepository;
import com.example.demo.mongo.service.quiz.GeminiAIUtils;
import com.example.demo.mongo.service.quiz.GeminiAIUtils.GeminiResponse;
import com.example.demo.mongo.service.quiz.QuizPromptBuilder;
import com.example.demo.modules.document.processing.api.DocumentProcessingFacade;
import com.example.demo.sql.dto.form.FormSession;
import com.example.demo.sql.entity.NormalUser;
import com.example.demo.sql.entity.User;
import com.example.demo.sql.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for identifying and ingesting community-contributed questions using
 * AI.
 * 
 * <p>
 * Loại bỏ việc phân tích file Excel thủ công, thay thế bằng việc sử dụng AI
 * (Gemini)
 * để trích xuất câu hỏi từ bất kỳ định dạng tài liệu nào (PDF, Word, Ảnh).
 *
 * @since 2.1
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BulkQuestionUploadService {

        private final QuestionBankRepository questionBankRepository;
        private final UserRepository userRepository;
        private final DocumentProcessingFacade documentProcessingFacade;
        private final QuizPromptBuilder promptBuilder;
        private final GeminiAIUtils geminiAIService;
        private final org.springframework.data.redis.core.RedisTemplate<String, String> redisTemplate;
        private final com.google.gson.Gson gson;

        private static final String SESSION_KEY_PREFIX = "form_session:";
        private static final long SESSION_TTL_SECONDS = 900; // 15 minutes

        /**
         * Extracts questions from a document and stages them in Redis under a
         * sessionId.
         */
        public List<Question> stageQuestions(MultipartFile file, String username, String sessionId) throws Exception {
                // 1. Level Check
                User user = userRepository.findByUserName(username)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                if (user instanceof NormalUser normalUser && normalUser.getCurrentTier() != null 
                    && "RESTRICTED".equals(normalUser.getCurrentTier().getId())) {
                        throw new RuntimeException("User is restricted from uploading content.");
                }

                // 2. AI Extraction via modular Processing Facade
                String extractedText = documentProcessingFacade.processDocument(file).getRawText();
                String prompt = promptBuilder.buildIdentificationPrompt(extractedText);
                GeminiResponse aiResponse = geminiAIService.generateIdentifiedQuestions(prompt);

                // 3. Stage in Redis (Overwrite if exists)
                FormSession session = FormSession.builder()
                                .sessionId(sessionId)
                                .ownerName(username)
                                .questions(aiResponse.getQuestions())
                                .createdAt(System.currentTimeMillis())
                                .build();

                String json = gson.toJson(session);
                redisTemplate.opsForValue().set(SESSION_KEY_PREFIX + sessionId, json,
                                Duration.ofSeconds(SESSION_TTL_SECONDS));

                log.info("Staged {} questions in Redis for session {} (User: {})",
                                aiResponse.getQuestions().size(), sessionId, username);
                
                return aiResponse.getQuestions();
        }


        /**
         * Commits a list of questions to the permanent bank.
         *
         * @param questions the list of questions to save
         * @param username  the contributor's username
         * @param contentId the shared content ID for this set of questions
         * @return saved list of QuestionBank entities
         */
        public List<QuestionBank> commitStagedQuestions(List<Question> questions, String username, String contentId) {
                List<QuestionBank> bankEntries = new ArrayList<>();
                for (Question question : questions) {
                        bankEntries.add(QuestionBank.builder()
                                        .contributorId(username)
                                        .contentId(contentId)
                                        .isCommunitySourced(true)
                                        .verificationStatus(VerificationStatus.REVIEWING)
                                        .questionData(question)
                                        .difficulty(0.0)
                                        .build());
                }

                return questionBankRepository.saveAll(bankEntries);
        }

        /**
         * Retrieves staged questions from Redis for a given sessionId.
         *
         * @param sessionId the unique session identifier
         * @return list of questions currently staged
         */
        public List<Question> getStagedQuestions(String sessionId) {
                String json = redisTemplate.opsForValue().get(SESSION_KEY_PREFIX + sessionId);
                if (json == null) {
                        throw new RuntimeException("Session not found or expired.");
                }

                FormSession session = gson.fromJson(json, FormSession.class);
                return session.getQuestions();
        }

        /**
         * Updates the list of staged questions in Redis.
         *
         * @param sessionId the unique session identifier
         * @param updatedQuestions the new list of questions
         * @param username the user performing the update
         */
        public void updateStagedQuestions(String sessionId, List<Question> updatedQuestions, String username) {
                String json = redisTemplate.opsForValue().get(SESSION_KEY_PREFIX + sessionId);
                if (json == null) {
                        throw new RuntimeException("Session not found or expired.");
                }

                FormSession session = gson.fromJson(json, FormSession.class);
                
                // Security check: ensure only the owner can update
                if (!session.getOwnerName().equals(username)) {
                        throw new RuntimeException("Unauthorized: You do not own this session.");
                }

                session.setQuestions(updatedQuestions);
                String updatedJson = gson.toJson(session);
                
                // Update in Redis, preserving remaining TTL if possible (resetting to 15m for simplicity here as per original design)
                redisTemplate.opsForValue().set(SESSION_KEY_PREFIX + sessionId, updatedJson,
                                Duration.ofSeconds(SESSION_TTL_SECONDS));

                log.info("Updated {} questions in Redis for session {} (User: {})",
                                updatedQuestions.size(), sessionId, username);
        }
}
