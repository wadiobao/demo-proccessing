package com.example.demo.modules.quiz.archive.infrastructure.port;

import java.util.List;
import java.util.Optional;

import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.ArchivedQuestionMongoEntity;

/**
 * Port for accessing the Quiz Archive (Persistence and external Cloudinary).
 */
public interface ArchivePort {
    
    ArchivedQuestionMongoEntity save(ArchivedQuestionMongoEntity archive);

    List<ArchivedQuestionMongoEntity> findByAuthor(String author);

    List<ArchivedQuestionMongoEntity> findAll();

    Optional<ArchivedQuestionMongoEntity> findById(String id);

    Optional<ArchivedQuestionMongoEntity> findOldestByAuthor(String author);

    void delete(ArchivedQuestionMongoEntity archive);

    long countByAuthor(String author);
}
