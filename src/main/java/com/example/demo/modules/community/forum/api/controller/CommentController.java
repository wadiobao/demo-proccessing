package com.example.demo.modules.community.forum.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.community.forum.api.dto.CommentRequest;
import com.example.demo.modules.community.forum.application.usecase.command.AddCommentUseCase;
import com.example.demo.modules.community.forum.application.usecase.command.DeleteCommentUseCase;
import com.example.demo.modules.community.forum.application.usecase.command.UpdateCommentUseCase;
import com.example.demo.modules.community.forum.application.usecase.command.DeleteUserCommentUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/comment")
@RequiredArgsConstructor
public class CommentController {

    private final AddCommentUseCase addCommentUseCase;
    private final DeleteCommentUseCase deleteCommentUseCase;
    private final UpdateCommentUseCase updateCommentUseCase;
    private final DeleteUserCommentUseCase deleteUserCommentUseCase;

    @PostMapping("/newComment/{formId}")
    public ResponseEntity<StateResponse<Object>> newComment(
            @PathVariable("formId") String formId, 
            @RequestBody @Valid CommentRequest request) {
        return ResponseEntity.ok(addCommentUseCase.execute(formId, request));
    }

    @DeleteMapping("/delete/{commentId}")
    public ResponseEntity<StateResponse<Object>> deleteComment(@PathVariable("commentId") String commentId) {
        return ResponseEntity.ok(deleteCommentUseCase.execute(commentId));
    }

    @PutMapping("/update/{commentId}")
    public ResponseEntity<StateResponse<Object>> updateComment(
            @PathVariable("commentId") String commentId,
            @RequestBody @Valid CommentRequest request) {
        return ResponseEntity.ok(updateCommentUseCase.execute(commentId, request));
    }

    @DeleteMapping("/delete-user/{commentId}")
    public ResponseEntity<StateResponse<Object>> deleteUserComment(@PathVariable("commentId") String commentId) {
        return ResponseEntity.ok(deleteUserCommentUseCase.execute(commentId));
    }
}
