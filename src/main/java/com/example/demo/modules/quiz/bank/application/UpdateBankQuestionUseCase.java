package com.example.demo.modules.quiz.bank.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.exception.HandleException;
import com.example.demo.enums.ErrorCode;
import com.example.demo.modules.quiz.bank.infrastructure.port.AuthorReputationPort;
import com.example.demo.modules.quiz.bank.infrastructure.port.BankPort;
import com.example.demo.modules.quiz.bank.infrastructure.port.EditQuotaPort;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.QuestionBankMongoEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Use case for contributors to update an existing question in the bank.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateBankQuestionUseCase {

    private final BankPort bankPort;
    private final AuthorReputationPort reputationPort;
    private final EditQuotaPort quotaPort;

    @Transactional
    public QuestionBankMongoEntity execute(String questionId, QuestionBankMongoEntity updatedData, String username) {
        // 1. Check Reputation
        if (!reputationPort.isAuthorizedToEdit(username)) {
            throw new RuntimeException("Insufficient reputation to edit the Question Bank.");
        }

        // 2. Check Quota
        if (quotaPort.hasExceededQuota(username)) {
            throw new RuntimeException("Daily edit quota reached.");
        }

        // 3. Find and Update
        QuestionBankMongoEntity existing = bankPort.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found with ID: " + questionId));

        existing.setQuestionData(updatedData.getQuestionData());
        existing.setContributorId(username);

        QuestionBankMongoEntity saved = bankPort.save(existing);

        // 4. Increment Quota
        quotaPort.incrementQuota(username);
        
        log.info("User {} updated question {}. Progress: {}/5", username, questionId, quotaPort.getCurrentCount(username));

        return saved;
    }
}
