package com.example.demo.modules.quiz.shared.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.ArchivedSessionMongoEntity;

@Repository
public interface ArchivedQuestionRepository extends MongoRepository<ArchivedSessionMongoEntity, String> {
    
    List<ArchivedSessionMongoEntity> findAllByAuthorOrderByCreatedAtDesc(String author);

    Optional<ArchivedSessionMongoEntity> findFirstByAuthorOrderByCreatedAtAsc(String author);

    long countByAuthor(String author);

    Optional<ArchivedSessionMongoEntity> findByAuthorAndTitle(String author, String title);
}
