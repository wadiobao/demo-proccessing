package com.example.demo.modules.quiz.bank.application;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.enums.VerificationStatus;
import com.example.demo.modules.quiz.bank.infrastructure.port.BankPort;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.QuestionBankMongoEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Administrative use case for promoting questions generated from a specific content to VERIFIED status.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PromoteBankQuestionsUseCase {

    private final BankPort bankPort;

    /**
     * Finds and promotes all questions with matching contentId.
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void execute(String contentId) {
        if (contentId == null || contentId.isEmpty()) {
            return;
        }

        log.info("Admin promoting all questions for contentId: {}", contentId);
        
        List<QuestionBankMongoEntity> questions = bankPort.findByContentId(contentId);
        
        int count = 0;
        for (QuestionBankMongoEntity q : questions) {
            if (VerificationStatus.REVIEWING.equals(q.getVerificationStatus())) {
                q.setVerificationStatus(VerificationStatus.VERIFIED);
                count++;
            }
        }
        
        if (count > 0) {
            bankPort.saveAll(questions);
            log.info("Successfully promoted {} questions for contentId: {}", count, contentId);
        }
    }
}
