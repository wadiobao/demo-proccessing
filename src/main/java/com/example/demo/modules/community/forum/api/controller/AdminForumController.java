package com.example.demo.modules.community.forum.api.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.community.forum.application.usecase.command.DeleteCommentUseCase;
import com.example.demo.modules.quiz.archive.infrastructure.port.ArchivePort;
import com.example.demo.modules.quiz.bank.application.usecase.PromoteBankQuestionsUseCase;
import com.example.demo.modules.quiz.bank.application.usecase.SearchBankQuestionsUseCase;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.ArchivedQuestionMongoEntity;

import lombok.RequiredArgsConstructor;

/**
 * Controller for Administrative forum and question management.
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
@PreAuthorize("hasRole('ADMIN')")
public class AdminForumController {

    private final DeleteCommentUseCase deleteCommentUseCase;
    private final ArchivePort archivePort;
    private final SearchBankQuestionsUseCase searchBankQuestionsUseCase;
    private final PromoteBankQuestionsUseCase promoteBankQuestionsUseCase;

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<StateResponse<Object>> deleteComment(@PathVariable String commentId) {
        return ResponseEntity.ok(deleteCommentUseCase.execute(commentId));
    }

    @GetMapping("/questions/archived")
    public ResponseEntity<StateResponse<List<ArchivedQuestionMongoEntity>>> getArchivedQuestions() {
        return ResponseEntity.ok(StateResponse.<List<ArchivedQuestionMongoEntity>>builder()
                .result(archivePort.findAll())
                .build());
    }

    @DeleteMapping("/questions/archived/{author}")
    public ResponseEntity<StateResponse<Object>> deleteArchivedQuestions(@PathVariable String author) {
        archivePort.deleteAllByAuthor(author);
        return ResponseEntity.ok(StateResponse.builder().message("Archived questions for author " + author + " deleted").build());
    }

    @GetMapping("/questions")
    public ResponseEntity<StateResponse<Object>> getQuestionBank(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(StateResponse.builder()
                .result(searchBankQuestionsUseCase.findAll(PageRequest.of(page, size)))
                .build());
    }

    @PutMapping("/questions/promote/{contentId}")
    public ResponseEntity<StateResponse<Object>> promoteQuestion(@PathVariable String contentId) {
        promoteBankQuestionsUseCase.execute(contentId);
        return ResponseEntity.ok(StateResponse.builder().message("Questions promoted successfully").build());
    }
}
