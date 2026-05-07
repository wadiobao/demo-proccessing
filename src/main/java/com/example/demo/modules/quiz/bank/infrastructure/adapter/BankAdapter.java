package com.example.demo.modules.quiz.bank.infrastructure.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.example.demo.modules.quiz.bank.infrastructure.port.BankPort;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.QuestionBankMongoEntity;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.repository.QuestionBankRepository;

import lombok.RequiredArgsConstructor;

/**
 * Adapter for managing Question Bank persistence.
 */
@Component
@RequiredArgsConstructor
public class BankAdapter implements BankPort {

    private final QuestionBankRepository repository;

    @Override
    public Optional<QuestionBankMongoEntity> findById(String id) {
        return repository.findById(id);
    }

    @Override
    public QuestionBankMongoEntity save(QuestionBankMongoEntity entity) {
        return repository.save(entity);
    }

    @Override
    public List<QuestionBankMongoEntity> saveAll(List<QuestionBankMongoEntity> entities) {
        return repository.saveAll(entities);
    }

    @Override
    public List<QuestionBankMongoEntity> findByContentId(String contentId) {
        return repository.findAllByContentId(contentId);
    }

    @Override
    public Page<QuestionBankMongoEntity> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public Page<QuestionBankMongoEntity> searchByKeyword(String keyword, Pageable pageable) {
        return repository.searchByKeyword(keyword, pageable);
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }
}
