package com.example.demo.modules.quiz.archive.api;

import java.util.List;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.ArchivedQuestionMongoEntity;

/**
 * Facade for Quiz Archiving.
 */
public interface ArchiveFacade {
    ArchivedQuestionMongoEntity createArchive(ArchivedQuestionMongoEntity archive);
    StateResponse<Object> getAuthorHistory(String author);
    void deleteArchive(String id, String username, boolean isAdmin);
    List<ArchivedQuestionMongoEntity> getAllArchives();
}
