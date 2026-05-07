package com.example.demo.modules.quiz.bank.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.modules.quiz.bank.infrastructure.port.AuthorReputationPort;
import com.example.demo.modules.quiz.bank.infrastructure.port.BankPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Use case for deleting questions from the bank.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeleteBankQuestionUseCase {

    private final BankPort bankPort;
    private final AuthorReputationPort reputationPort;

    @Transactional
    public void execute(String id, String username) {
        // 1. Check Reputation (Requires Expert/Moderator/Admin)
        if (!reputationPort.isAuthorizedToEdit(username)) {
            throw new RuntimeException("Insufficient reputation to delete from the Question Bank.");
        }

        // 2. Find and Delete
        if (!bankPort.findById(id).isPresent()) {
            throw new RuntimeException("Question not found with ID: " + id);
        }

        bankPort.deleteById(id);
        log.info("User {} deleted question {} from Question Bank", username, id);
    }
}
