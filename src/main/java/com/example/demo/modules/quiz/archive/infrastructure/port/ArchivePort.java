package com.example.demo.modules.quiz.archive.infrastructure.port;

import java.util.List;
import java.util.Optional;

import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.ArchivedSessionMongoEntity;

/**
 * Port for accessing the Quiz Archive (Persistence and external Cloudinary).
 */
public interface ArchivePort {
    
    ArchivedSessionMongoEntity save(ArchivedSessionMongoEntity archive);

    List<ArchivedSessionMongoEntity> findByAuthor(String author);

    List<ArchivedSessionMongoEntity> findAll();

    Optional<ArchivedSessionMongoEntity> findById(String id);

    Optional<ArchivedSessionMongoEntity> findOldestByAuthor(String author);

    void delete(ArchivedSessionMongoEntity archive);

    void deleteAllByAuthor(String author);

    long countByAuthor(String author);
}
