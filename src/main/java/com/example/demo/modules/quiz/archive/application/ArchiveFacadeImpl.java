package com.example.demo.modules.quiz.archive.application;

import com.example.demo.modules.quiz.archive.api.ArchiveFacade;

import java.util.List;

import org.springframework.stereotype.Service;


import com.example.demo.dto.StateResponse;
import com.example.demo.modules.quiz.archive.api.ArchiveFacade;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.ArchivedSessionMongoEntity;

import lombok.RequiredArgsConstructor;

/**
 * Implementation of ArchiveFacade.
 */
@Service
@RequiredArgsConstructor
class ArchiveFacadeImpl implements ArchiveFacade {

    private final ArchiveQuizUseCase archiveQuizUseCase;
    private final GetArchiveUseCase getArchiveUseCase;
    private final DeleteArchiveUseCase deleteArchiveUseCase;
    private final AdminArchiveUseCase adminArchiveUseCase;

    @Override
    public ArchivedSessionMongoEntity createArchive(ArchivedSessionMongoEntity archive) {
        return archiveQuizUseCase.execute(archive);
    }

    @Override
    public StateResponse<Object> getAuthorHistory(String author) {
        return getArchiveUseCase.findByAuthor(author);
    }

    @Override
    public void deleteArchive(String id, String username, boolean isAdmin) {
        deleteArchiveUseCase.execute(id, username, isAdmin);
    }

    @Override
    public List<ArchivedSessionMongoEntity> getAllArchives() {
        return adminArchiveUseCase.findAll();
    }
}
