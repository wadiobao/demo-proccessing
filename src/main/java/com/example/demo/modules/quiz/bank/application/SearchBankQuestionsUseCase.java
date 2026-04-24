package com.example.demo.modules.quiz.bank.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.demo.modules.quiz.bank.infrastructure.port.BankPort;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.QuestionBankMongoEntity;

import lombok.RequiredArgsConstructor;

/**
 * Use case for searching and listing questions in the bank.
 */
@Service
@RequiredArgsConstructor
public class SearchBankQuestionsUseCase {

    private final BankPort bankPort;

    public Page<QuestionBankMongoEntity> findAll(Pageable pageable) {
        return bankPort.findAll(pageable);
    }

    public Page<QuestionBankMongoEntity> search(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return bankPort.findAll(pageable);
        }
        return bankPort.searchByKeyword(keyword, pageable);
    }
}
