package com.example.demo.modules.quiz.generation.application;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.enums.VerificationStatus;
import com.example.demo.modules.document.processing.api.DocumentProcessingFacade;
import com.example.demo.modules.quiz.shared.domain.model.Question;
import com.example.demo.modules.quiz.generation.infrastructure.port.AiGenerationPort;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.QuestionBankMongoEntity;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.repository.QuestionBankRepository;
import com.example.demo.sql.dto.form.FormSession;
import com.example.demo.sql.entity.NormalUser;
import com.example.demo.sql.entity.User;
import com.example.demo.sql.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for identifying and ingesting community-contributed questions using
 * AI.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BulkQuestionUploadService {

    private final QuestionBankRepository questionBankRepository;
    private final UserRepository userRepository;
    private final DocumentProcessingFacade documentProcessingFacade;
    private final QuizPromptBuilder promptBuilder;
    private final AiGenerationPort aiGenerationPort;
    private final org.springframework.data.redis.core.RedisTemplate<String, String> redisTemplate;
    private final com.google.gson.Gson gson;

    private static final String SESSION_KEY_PREFIX = "form_session:";
    private static final long SESSION_TTL_SECONDS = 900; // 15 minutes

    /**
     * Extracts questions from a document and stages them in Redis.
     */
    public List<Question> stageQuestions(MultipartFile file, String username, String sessionId) throws Exception {
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user instanceof NormalUser normalUser && normalUser.getCurrentTier() != null
                && "RESTRICTED".equals(normalUser.getCurrentTier().getId())) {
            throw new RuntimeException("User is restricted from uploading content.");
        }

        String extractedText = documentProcessingFacade.processDocument(file).getRawText();
        QuizPromptBuilder.PromptContext context = promptBuilder.buildIdentificationPrompt(extractedText);

        // AiGenerationPort returns record AiResponse(String status, List<Question> questions)
        List<Question> questions = aiGenerationPort.generateIdentifiedQuestions(context.instruction(), context.userMessage()).questions();

        FormSession session = FormSession.builder()
                .sessionId(sessionId)
                .ownerName(username)
                .questions(questions) // FormSession now uses Domain Question
                .createdAt(System.currentTimeMillis())
                .build();

        String json = gson.toJson(session);
        redisTemplate.opsForValue().set(SESSION_KEY_PREFIX + sessionId, json, Duration.ofSeconds(SESSION_TTL_SECONDS));

        return questions;
    }

    /**
     * Commits staged questions to the permanent bank.
     */
    public List<QuestionBankMongoEntity> commitStagedQuestions(List<Question> questions, String username,
            String contentId) {
        List<QuestionBankMongoEntity> bankEntries = new ArrayList<>();
        for (Question question : questions) {
            bankEntries.add(QuestionBankMongoEntity.builder()
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

    public List<Question> getStagedQuestions(String sessionId) {
        String json = redisTemplate.opsForValue().get(SESSION_KEY_PREFIX + sessionId);
        if (json == null)
            throw new RuntimeException("Session not found or expired.");

        FormSession session = gson.fromJson(json, FormSession.class);
        return session.getQuestions();
    }

    public void updateStagedQuestions(String sessionId, List<Question> updatedQuestions, String username) {
        String json = redisTemplate.opsForValue().get(SESSION_KEY_PREFIX + sessionId);
        if (json == null)
            throw new RuntimeException("Session not found or expired.");

        FormSession session = gson.fromJson(json, FormSession.class);
        if (!session.getOwnerName().equals(username))
            throw new RuntimeException("Unauthorized");

        session.setQuestions(updatedQuestions);
        redisTemplate.opsForValue().set(SESSION_KEY_PREFIX + sessionId, gson.toJson(session),
                Duration.ofSeconds(SESSION_TTL_SECONDS));
    }
}
