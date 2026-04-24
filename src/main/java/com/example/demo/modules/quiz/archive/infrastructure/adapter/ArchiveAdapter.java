package com.example.demo.modules.quiz.archive.infrastructure.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.example.demo.modules.quiz.archive.infrastructure.port.ArchivePort;
import com.example.demo.modules.quiz.shared.domain.model.Question;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.ArchivedQuestionMongoEntity;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.repository.ArchivedQuestionRepository;
import com.example.demo.utils.CloudinaryUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Adapter implementing the ArchivePort using MongoDB and Cloudinary.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ArchiveAdapter implements ArchivePort {

    private final ArchivedQuestionRepository repository;
    private final CloudinaryUtils cloudinaryUtils;

    @Override
    public ArchivedQuestionMongoEntity save(ArchivedQuestionMongoEntity archive) {
        return repository.save(archive);
    }

    @Override
    public List<ArchivedQuestionMongoEntity> findByAuthor(String author) {
        return repository.findAllByAuthorOrderByCreatedAtDesc(author);
    }

    @Override
    public List<ArchivedQuestionMongoEntity> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<ArchivedQuestionMongoEntity> findById(String id) {
        return repository.findById(id);
    }

    @Override
    public Optional<ArchivedQuestionMongoEntity> findOldestByAuthor(String author) {
        return repository.findFirstByAuthorOrderByCreatedAtAsc(author);
    }

    @Override
    public void delete(ArchivedQuestionMongoEntity archive) {
        // 1. Cleanup Cloudinary images
        List<String> deleteImgList = new ArrayList<>();
        List<Question> questions = archive.getQuestions();
        if (questions != null) {
            for (Question question : questions) {
                if (question.getImgPublicId() != null) {
                    deleteImgList.add(question.getImgPublicId());
                }
            }
        }

        if (!deleteImgList.isEmpty()) {
            try {
                cloudinaryUtils.delete(deleteImgList);
                log.info("Deleted images from Cloudinary for archive {}: {}", archive.getId(), deleteImgList);
            } catch (Exception e) {
                log.error("Failed to delete images from Cloudinary: {}", e.getMessage());
                // Non-fatal, continue with DB deletion
            }
        }

        // 2. Delete from MongoDB
        repository.deleteById(archive.getId());
    }

    @Override
    public long countByAuthor(String author) {
        return repository.countByAuthor(author);
    }
}
