package com.example.demo.mongo.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.enums.Role;
import com.example.demo.enums.VerificationStatus;
import com.example.demo.mongo.entity.QuestionBank;
import com.example.demo.mongo.repository.QuestionBankRepository;
import com.example.demo.sql.entity.User;
import com.example.demo.sql.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing the Question Bank with community governance.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class QuestionBankService {

    QuestionBankRepository questionBankRepository;
    UserRepository userRepository;

    // In-memory quota tracker (should be moved to Redis for production/cluster)
    Map<String, Integer> dailyEditCounts = new HashMap<>();
    LocalDate lastQuotaReset = LocalDate.now();

    private static final int MAX_DAILY_EDITS = 5;

    /**
     * Updates an existing question if the user has sufficient reputation (EXPERT or
     * higher).
     */
    @Transactional
    public QuestionBank updateQuestion(String questionId, QuestionBank updatedData, String username) {
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 1. Level Check
        if (user.getCurrentTier() != Role.EXPERT && user.getCurrentTier() != Role.MODERATOR
                && user.getCurrentTier() != Role.ADMIN) {
            throw new RuntimeException("Insufficient reputation to edit the Question Bank.");
        }

        // 2. Quota Check
        checkAndResetQuota();
        int currentEdits = dailyEditCounts.getOrDefault(username, 0);
        if (currentEdits >= MAX_DAILY_EDITS && user.getCurrentTier() != Role.ADMIN) {
            throw new RuntimeException("Daily edit quota reached (max " + MAX_DAILY_EDITS + ").");
        }

        // 3. Perform Edit
        QuestionBank existing = questionBankRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        existing.setQuestionData(updatedData.getQuestionData());
        existing.setContributorId(username);

        QuestionBank saved = questionBankRepository.save(existing);

        // 4. Update Quota
        dailyEditCounts.put(username, currentEdits + 1);
        log.info("User {} edited question {}. Daily count: {}", username, questionId, currentEdits + 1);

        return saved;
    }

    private synchronized void checkAndResetQuota() {
        if (LocalDate.now().isAfter(lastQuotaReset)) {
            dailyEditCounts.clear();
            lastQuotaReset = LocalDate.now();
        }
    }

    /**
     * Officially verifies all questions linked to a contentId once community
     * threshold is met.
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
     * Performs a text-based search across all community questions.
     */
    public Page<QuestionBank> searchQuestions(String keyword,
            Pageable pageable) {
        return questionBankRepository.searchByKeyword(keyword, pageable);
    }
}
