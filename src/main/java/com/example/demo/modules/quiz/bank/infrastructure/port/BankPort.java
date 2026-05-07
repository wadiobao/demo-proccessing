package com.example.demo.modules.quiz.bank.infrastructure.port;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.QuestionBankMongoEntity;

/**
 * Port for administrative and community management of the Question Bank.
 */
public interface BankPort {

    Optional<QuestionBankMongoEntity> findById(String id);

    QuestionBankMongoEntity save(QuestionBankMongoEntity entity);

    List<QuestionBankMongoEntity> saveAll(List<QuestionBankMongoEntity> entities);

    List<QuestionBankMongoEntity> findByContentId(String contentId);

    Page<QuestionBankMongoEntity> findAll(Pageable pageable);

    Page<QuestionBankMongoEntity> searchByKeyword(String keyword, Pageable pageable);
    
    void deleteById(String id);
}
