package com.example.demo.mongo.service;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.enums.VerificationStatus;
import com.example.demo.mongo.entity.QuestionBank;
import com.example.demo.mongo.repository.QuestionBankRepository;
import com.example.demo.sql.entity.Admin;
import com.example.demo.sql.entity.NormalUser;
import com.example.demo.sql.entity.Tier;
import com.example.demo.sql.entity.User;
import com.example.demo.sql.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing the community-driven Question Bank operations.
 *
 * <p>This service handles question updates with reputation-based access control,
 * daily edit limits enforced via Redis, and administrative verification of content.
 * It serves as the core logic for the global question repository populated by users.
 *
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class QuestionBankService {

    QuestionBankRepository questionBankRepository;
    UserRepository userRepository;
    RedisTemplate<String, String> redisTemplate;

    private static final int MAX_DAILY_EDITS = 5;
    private static final String QUOTA_KEY_PREFIX = "quota:question_bank:edit:";

    /**
     * Updates an existing question in the bank while enforcing reputation and quota rules.
     *
     * <p>The method verifies if the user has the required Tier (EXPERT or MODERATOR) 
     * and checks if they have exceeded their daily edit limit stored in Redis.
     *
     * @param questionId the unique identifier of the question to be updated
     * @param updatedData the new question content and metadata
     * @param username the username of the contributor performing the edit
     * @return the saved QuestionBank instance reflecting the updates
     * @throws RuntimeException if the user is not found, has insufficient reputation, 
     *                          exceeds the daily quota, or the question does not exist
     */
    @Transactional
    public QuestionBank updateQuestion(String questionId, QuestionBank updatedData, String username) {
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 1. Level & Quota Check
        if (user instanceof NormalUser normalUser) {
            Tier currentTier = normalUser.getCurrentTier();
            String tierId = (currentTier != null) ? currentTier.getId() : "";
            
            if (!"EXPERT".equals(tierId) && !"MODERATOR".equals(tierId)) {
                throw new RuntimeException("Insufficient reputation to edit the Question Bank.");
            }
            
            String quotaKey = QUOTA_KEY_PREFIX + username + ":" + LocalDate.now();
            String currentEditsStr = redisTemplate.opsForValue().get(quotaKey);
            int currentEdits = (currentEditsStr != null) ? Integer.parseInt(currentEditsStr) : 0;

            if (currentEdits >= MAX_DAILY_EDITS) {
                throw new RuntimeException("Daily edit quota reached (max " + MAX_DAILY_EDITS + ").");
            }
            
            // Increment and set TTL (24h)
            redisTemplate.opsForValue().increment(quotaKey);
            redisTemplate.expire(quotaKey, java.time.Duration.ofDays(1));
        } else if (!(user instanceof Admin)) {
             throw new RuntimeException("Unauthorized to edit the Question Bank.");
        }

        // 3. Perform Edit
        QuestionBank existing = questionBankRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        existing.setQuestionData(updatedData.getQuestionData());
        existing.setContributorId(username);

        QuestionBank saved = questionBankRepository.save(existing);

        // 4. Logging
        String quotaKey = QUOTA_KEY_PREFIX + username + ":" + java.time.LocalDate.now();
        String currentEditsStr = redisTemplate.opsForValue().get(quotaKey);
        log.info("User {} edited question {}. Daily count: {}", username, questionId, currentEditsStr);

        return saved;
    }

    /**
     * Verifies and promotes all questions associated with a specific content identifier.
     *
     * <p>This administrative operation transitions question statuses from REVIEWING to VERIFIED
     * to indicate they have met community or system quality standards.
     *
     * @param contentId the source content ID from which the questions were generated
     */
    @Transactional
    public void promoteByContentId(String contentId) {
        if (contentId == null || contentId.isEmpty()) {
			return;
		}

        log.info("Promoting all questions for contentId {} to VERIFIED", contentId);
        // Find all for content
        java.util.List<QuestionBank> questions = questionBankRepository.findAll().stream()
                .filter(q -> contentId.equals(q.getContentId()))
                .toList();

        for (QuestionBank q : questions) {
            if (q.getVerificationStatus().equals(VerificationStatus.REVIEWING)) {
                q.setVerificationStatus(VerificationStatus.VERIFIED);
            }
        }
        questionBankRepository.saveAll(questions);
    }

    /**
     * Retrieves a paginated list of all questions currently stored in the Question Bank.
     *
     * @param pageable the pagination and sorting information
     * @return a Page of QuestionBank entities
     */
    public Page<QuestionBank> findAll(Pageable pageable) {
        return questionBankRepository.findAll(pageable);
    }

    /**
     * Searches for questions within the bank based on a text-based keyword.
     *
     * <p>Uses MongoDB text search capabilities to find matches across indexed fields.
     *
     * @param keyword the search term used to match questions
     * @param pageable the pagination and sorting information
     * @return a Page of matching QuestionBank entities
     */
    public Page<QuestionBank> searchQuestions(String keyword,
            Pageable pageable) {
        return questionBankRepository.searchByKeyword(keyword, pageable);
    }
}
