package com.example.demo.modules.quiz.shared.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.ArchivedQuestionMongoEntity;

@Repository
public interface ArchivedQuestionRepository extends MongoRepository<ArchivedQuestionMongoEntity, String> {
    
    List<ArchivedQuestionMongoEntity> findAllByAuthorOrderByCreatedAtDesc(String author);

    Optional<ArchivedQuestionMongoEntity> findFirstByAuthorOrderByCreatedAtAsc(String author);

    long countByAuthor(String author);

    Optional<ArchivedQuestionMongoEntity> findByAuthorAndTitle(String author, String title);
}
