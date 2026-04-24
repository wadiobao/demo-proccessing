package com.example.demo.modules.quiz.shared.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.QuestionBankMongoEntity;

@Repository
public interface QuestionBankRepository extends MongoRepository<QuestionBankMongoEntity, String> {

    long countByContentId(String contentId);

    List<QuestionBankMongoEntity> findAllByContentId(String contentId);

    Optional<QuestionBankMongoEntity> findByContentIdAndQuestionHash(String contentId, String questionHash);

    @Aggregation(pipeline = {
        "{ $match: { contentId: ?0 } }",
        "{ $sample: { size: ?1 } }"
    })
    List<QuestionBankMongoEntity> findRandomByContentId(String contentId, int size);

    @Query("{$text: {$search: ?0}}")
    Page<QuestionBankMongoEntity> searchByKeyword(String keyword, Pageable pageable);
}
