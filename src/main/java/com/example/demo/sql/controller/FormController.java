package com.example.demo.sql.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.StateResponse;
import com.example.demo.mongo.service.BulkQuestionUploadService;
import com.example.demo.sql.dto.form.FormRequest;
import com.example.demo.sql.dto.form.TopicRequest;
import com.example.demo.sql.entity.Form;
import com.example.demo.sql.entity.User;
import com.example.demo.sql.repository.FormRepository;
import com.example.demo.sql.repository.UserRepository;
import com.example.demo.sql.service.ReputationService;
import com.example.demo.sql.service.iservice.IFormService;

import lombok.RequiredArgsConstructor;

@RestController
@CrossOrigin(origins = "${app.cors.allowed-origins}")
@RequestMapping("/api/v1/discussion")
@RequiredArgsConstructor
public class FormController {

	private final IFormService formService;
	private final UserRepository userRepository;
	private final FormRepository formRepository;
	private final ReputationService reputationService;
	private final BulkQuestionUploadService bulkQuestionUploadService;

	@GetMapping
	public ResponseEntity<StateResponse<Object>> getAllTopic(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return ResponseEntity.ok(formService.getAllTopic(PageRequest.of(page, size)));
	}

	@GetMapping("/topics")
	public ResponseEntity<StateResponse<Object>> getAllTopics(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return ResponseEntity.ok(formService.getAllTopics(PageRequest.of(page, size)));
	}

	@GetMapping("/{topicId}/forms")
	public ResponseEntity<StateResponse<Object>> getAllFormFromTopic(@PathVariable("topicId") Long topicId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return ResponseEntity.ok(formService.getAllFormFromTopic(topicId, PageRequest.of(page, size)));
	}

	@GetMapping("/forms")
	public ResponseEntity<StateResponse<Object>> getAllForm(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return ResponseEntity.ok(formService.getAllForm(PageRequest.of(page, size)));
	}

	@GetMapping("/session")
	public ResponseEntity<StateResponse<Object>> startSession() {
		String username = SecurityContextHolder.getContext()
				.getAuthentication().getName();
		return ResponseEntity.ok(StateResponse.builder().result(formService.startSession(username)).build());
	}

	@DeleteMapping("/session/{sessionId}")
	public ResponseEntity<StateResponse<Object>> discardSession(@PathVariable String sessionId) {
		String username = SecurityContextHolder.getContext()
				.getAuthentication().getName();
		formService.discardSession(sessionId, username);
		return ResponseEntity.ok(StateResponse.builder().message("Session discarded").build());
	}

	@PostMapping("/upload-questions")
	public ResponseEntity<StateResponse<Object>> uploadQuestions(@RequestParam("file") MultipartFile file,
			@RequestParam("sessionId") String sessionId) throws Exception {
		String username = SecurityContextHolder.getContext()
				.getAuthentication().getName();
		bulkQuestionUploadService.stageQuestions(file, username, sessionId);
		return ResponseEntity.ok(StateResponse.builder().message("Questions staged successfully").build());
	}

	@PostMapping(value = "/{topicid}/newform")
	public ResponseEntity<StateResponse<Object>> newForm(@PathVariable("topicid") Long topicId,
			@RequestPart("formRequest") FormRequest formRequest,
			@RequestParam(value = "sessionId", required = false) String sessionId) {
		return ResponseEntity.ok(formService.newForm(topicId, formRequest, sessionId));
	}

	@GetMapping("/form/{formId}")
	public ResponseEntity<StateResponse<Object>> getFormComment(@PathVariable("formId") String formId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return ResponseEntity.ok(formService.getFormComment(formId, PageRequest.of(page, size)));
	}

	@PostMapping("/newtopic")
	public ResponseEntity<StateResponse<Object>> newTopic(@RequestBody TopicRequest request) {
		return ResponseEntity.ok(formService.newTopic(request));
	}

	@DeleteMapping("/delete/{formid}")
	public ResponseEntity<StateResponse<Object>> deleteComment(@PathVariable("formid") String formid) {
		return ResponseEntity.ok(formService.deleteForm(formid));
	}

	/**
	 * Casts a vote on a community discussion post.
	 */
	@PostMapping("/{formId}/vote")
	public ResponseEntity<StateResponse<Object>> vote(
			@PathVariable("formId") String formId,
			@RequestParam("value") int value) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		User voter = userRepository.findByUserName(username)
				.orElseThrow(() -> new RuntimeException("User not found"));
		Form post = formRepository.findById(formId)
				.orElseThrow(() -> new RuntimeException("Post not found"));

		reputationService.castVote(voter, post, value);

		return ResponseEntity.ok(StateResponse.builder().message("Vote recorded successfully").build());
	}


	/**
	 * Search for discussions using Full-Text Search.
	 */
	@GetMapping("/search")
	public ResponseEntity<StateResponse<Object>> search(
			@RequestParam("keyword") String keyword,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return ResponseEntity.ok(formService.searchByKeyword(keyword, PageRequest.of(page, size)));
	}
}
