package com.example.demo.sql.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.StateResponse;
import com.example.demo.sql.dto.form.FormRequest;
import com.example.demo.sql.dto.form.TopicRequest;
import com.example.demo.sql.service.iservice.IFormService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@CrossOrigin(origins = "${app.cors.allowed-origins}")
@RequestMapping("/api/v1/discussion")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FormController {

	IFormService formService;

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

	@PostMapping("/{topicid}/newform")
	public ResponseEntity<StateResponse<Object>> newForm(@PathVariable("topicid") Long topicId,
			@RequestBody FormRequest formRequest) {
		return ResponseEntity.ok(formService.newForm(topicId, formRequest));
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

}
