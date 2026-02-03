package com.example.demo.sql.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.StateResponse;
import com.example.demo.sql.dto.form.CommentRequest;
import com.example.demo.sql.service.iservice.ICommentService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@CrossOrigin(origins = "${app.cors.allowed-origins}")
@RequestMapping("/api/v1/discussion/comment")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentController {

	ICommentService commentService;

	@PostMapping("/{formId}")
	public ResponseEntity<StateResponse<Object>> newComment(@PathVariable("formId") String formId,
			@RequestBody CommentRequest request) {
		return ResponseEntity.ok(commentService.newComment(formId, request));
	}

	@DeleteMapping("/delete/{commentid}")
	public ResponseEntity<StateResponse<Object>> deleteComment(@PathVariable("commentid") String commentId) {
		return ResponseEntity.ok(commentService.deleteComment(commentId));
	}

}
