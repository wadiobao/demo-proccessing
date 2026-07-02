package com.example.demo.modules.quiz.archive.application;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.modules.quiz.archive.infrastructure.port.ArchivePort;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.ArchivedSessionMongoEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Use case for archiving a newly generated or uploaded quiz.
 * Enforces a limit of 6 archived quizzes per user.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ArchiveQuizUseCase {

    private final ArchivePort archivePort;

    @Transactional
    public ArchivedSessionMongoEntity execute(ArchivedSessionMongoEntity archive) {
        log.info("Archiving quiz '{}' for author: {}", archive.getTitle(), archive.getAuthor());

        // 1. Enforce limit: Max 6 archives per user
        if (archivePort.countByAuthor(archive.getAuthor()) >= 6) {
            log.info("User {} reached archive limit. Deleting oldest record.", archive.getAuthor());
            archivePort.findOldestByAuthor(archive.getAuthor())
                    .ifPresent(oldest -> archivePort.delete(oldest));
        }

        // 2. Set metadata
        archive.setCreatedAt(LocalDateTime.now());
        
        // 3. Save
        return archivePort.save(archive);
    }
}
