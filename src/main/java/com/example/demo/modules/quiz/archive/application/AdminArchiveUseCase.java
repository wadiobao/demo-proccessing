package com.example.demo.modules.quiz.archive.application;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.example.demo.modules.quiz.archive.infrastructure.port.ArchivePort;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.ArchivedQuestionMongoEntity;

import lombok.RequiredArgsConstructor;

/**
 * Use case for admin-level operations on the quiz archive.
 */
@Service
@RequiredArgsConstructor
public class AdminArchiveUseCase {

    private final ArchivePort archivePort;

    /**
     * Retrieves all archived records across all users.
     */
    @PreAuthorize("hasRole('ADMIN')")
    public List<ArchivedQuestionMongoEntity> findAll() {
        return archivePort.findAll();
    }
}
