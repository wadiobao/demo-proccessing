package com.example.demo.modules.quiz.archive.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.enums.ErrorCode;
import com.example.demo.exception.HandleException;
import com.example.demo.modules.quiz.archive.infrastructure.port.ArchivePort;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.ArchivedSessionMongoEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Use case for deleting an archived quiz record and cleaning up associated images.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeleteArchiveUseCase {

    private final ArchivePort archivePort;

    /**
     * Deletes a specific archive by ID, ensuring ownership.
     */
    @Transactional
    public void execute(String archiveId, String requesterUsername, boolean isAdmin) {
        ArchivedSessionMongoEntity archive = archivePort.findById(archiveId)
                .orElseThrow(() -> new HandleException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!isAdmin && !archive.getAuthor().equals(requesterUsername)) {
            throw new HandleException(ErrorCode.UNAUTHORIZED);
        }

        log.info("Deleting archive: {} (Author: {}, Action by: {})", archiveId, archive.getAuthor(), requesterUsername);
        archivePort.delete(archive);
    }
}
