package com.example.demo.modules.quiz.archive.application;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.quiz.archive.api.dto.QuizArchiveResponse;
import com.example.demo.modules.quiz.archive.infrastructure.port.ArchivePort;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.ArchivedQuestionMongoEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Use case for retrieving archived quiz sessions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GetArchiveUseCase {

    private final ArchivePort archivePort;

    /**
     * Finds all archived quizzes for the given author.
     */
    public StateResponse<Object> findByAuthor(String author) {
        List<ArchivedQuestionMongoEntity> archives = archivePort.findByAuthor(author);
        
        List<QuizArchiveResponse> result = archives.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return StateResponse.builder()
                .result(result)
                .build();
    }

    private QuizArchiveResponse mapToResponse(ArchivedQuestionMongoEntity entity) {
        return QuizArchiveResponse.builder()
                .id(entity.getId())
                .author(entity.getAuthor())
                .title(entity.getTitle())
                .questions(entity.getQuestions())
                .createdAt(entity.getCreatedAt())
                .pdfBase64(entity.getPdfBase64())
                .wordBase64(entity.getWordBase64())
                .evaluated(entity.isEvaluated())
                .build();
    }
}
