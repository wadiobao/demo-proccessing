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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.community.forum.api.dto.TopicRequest;
import com.example.demo.modules.community.forum.application.usecase.command.AdminDeleteTopicUseCase;
import com.example.demo.modules.community.forum.application.usecase.command.AdminUpdateTopicUseCase;
import com.example.demo.modules.community.forum.application.usecase.command.DeleteCommentUseCase;
import com.example.demo.modules.community.forum.application.usecase.command.DeleteFormUseCase;
import com.example.demo.modules.community.forum.application.usecase.query.AdminGetAllCommentsUseCase;
import com.example.demo.modules.community.forum.application.usecase.query.AdminGetAllFormsUseCase;
import com.example.demo.modules.community.forum.application.usecase.query.AdminGetAllTopicsUseCase;
import com.example.demo.modules.quiz.archive.infrastructure.port.ArchivePort;
import com.example.demo.modules.quiz.bank.application.usecase.PromoteBankQuestionsUseCase;
import com.example.demo.modules.quiz.bank.application.usecase.SearchBankQuestionsUseCase;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.ArchivedSessionMongoEntity;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller for Administrative forum and question management.
 * All actions in this controller require the ADMIN role.
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

    // Use cases for forum content management by administration
    private final AdminGetAllTopicsUseCase adminGetAllTopicsUseCase;
    private final AdminUpdateTopicUseCase adminUpdateTopicUseCase;
    private final AdminDeleteTopicUseCase adminDeleteTopicUseCase;
    private final AdminGetAllFormsUseCase adminGetAllFormsUseCase;
    private final DeleteFormUseCase deleteFormUseCase;
    private final AdminGetAllCommentsUseCase adminGetAllCommentsUseCase;

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<StateResponse<Object>> deleteComment(@PathVariable String commentId) {
        return ResponseEntity.ok(deleteCommentUseCase.execute(commentId));
    }

    @GetMapping("/questions/archived")
    public ResponseEntity<StateResponse<List<ArchivedSessionMongoEntity>>> getArchivedQuestions() {
        return ResponseEntity.ok(StateResponse.<List<ArchivedSessionMongoEntity>>builder()
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

    /**
     * Retrieve all topics ordered by ID in ascending order (paginated).
     */
    @GetMapping("/topics")
    public ResponseEntity<StateResponse<Object>> getAllTopics(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminGetAllTopicsUseCase.execute(PageRequest.of(page, size)));
    }

    /**
     * Update an existing topic's name.
     */
    @PutMapping("/topics/{topicId}")
    public ResponseEntity<StateResponse<Object>> updateTopic(
            @PathVariable Long topicId,
            @RequestBody @Valid TopicRequest request) {
        return ResponseEntity.ok(adminUpdateTopicUseCase.execute(topicId, request));
    }

    /**
     * Delete a topic and cascade delete all forms and comments under it.
     */
    @DeleteMapping("/topics/{topicId}")
    public ResponseEntity<StateResponse<Object>> deleteTopic(@PathVariable Long topicId) {
        return ResponseEntity.ok(adminDeleteTopicUseCase.execute(topicId));
    }

    /**
     * Retrieve all forms (paginated, with optional author filter).
     */
    @GetMapping("/forms")
    public ResponseEntity<StateResponse<Object>> getAllForms(
            @RequestParam(required = false) String tacGia,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminGetAllFormsUseCase.execute(tacGia, PageRequest.of(page, size)));
    }

    /**
     * Delete any form by its ID (as an administrator).
     */
    @DeleteMapping("/forms/{formId}")
    public ResponseEntity<StateResponse<Object>> deleteForm(@PathVariable String formId) {
        return ResponseEntity.ok(deleteFormUseCase.execute(formId));
    }

    /**
     * Retrieve all comments (paginated).
     */
    @GetMapping("/comments")
    public ResponseEntity<StateResponse<Object>> getAllComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminGetAllCommentsUseCase.execute(PageRequest.of(page, size)));
    }
}
