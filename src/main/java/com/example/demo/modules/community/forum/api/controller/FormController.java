package com.example.demo.modules.community.forum.api.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.community.forum.api.dto.FormRequest;
import com.example.demo.modules.community.forum.api.dto.FormSession;
import com.example.demo.modules.community.forum.api.dto.TopicRequest;
import com.example.demo.modules.community.forum.application.usecase.command.CreateFormUseCase;
import com.example.demo.modules.community.forum.application.usecase.command.CreateTopicUseCase;
import com.example.demo.modules.community.forum.application.usecase.command.DeleteFormUseCase;
import com.example.demo.modules.community.forum.application.usecase.command.ManageFormSessionUseCase;
import com.example.demo.modules.community.forum.application.usecase.query.GetFormDetailUseCase;
import com.example.demo.modules.community.forum.application.usecase.query.GetFormVoteUseCase;
import com.example.demo.modules.community.forum.application.usecase.query.GetTopicTagsUseCase;
import com.example.demo.modules.community.forum.application.usecase.query.ListFormsUseCase;
import com.example.demo.modules.community.forum.application.usecase.query.ListTopicsUseCase;
import com.example.demo.modules.quiz.shared.domain.model.Question;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/form")
@RequiredArgsConstructor
public class FormController {

    private final ListFormsUseCase listFormsUseCase;
    private final CreateFormUseCase createFormUseCase;
    private final GetFormDetailUseCase getFormDetailUseCase;
    private final CreateTopicUseCase createTopicUseCase;
    private final ListTopicsUseCase listTopicsUseCase;
    private final GetTopicTagsUseCase getTopicTagsUseCase;
    private final DeleteFormUseCase deleteFormUseCase;
    private final GetFormVoteUseCase getFormVoteUseCase;
    private final ManageFormSessionUseCase manageFormSessionUseCase;

    @GetMapping("/getAll")
    public ResponseEntity<StateResponse<Object>> getAllForm(Pageable pageable) {
        return ResponseEntity.ok(listFormsUseCase.execute(pageable));
    }

    @PostMapping("/{topicId}/newForm")
    public ResponseEntity<StateResponse<Object>> newForm(
            @PathVariable Long topicId,
            @RequestPart @Valid FormRequest formRequest,
            @RequestParam(required = false) String sessionId) {
        return ResponseEntity.ok(createFormUseCase.execute(topicId, formRequest, sessionId));
    }

    @GetMapping("/{formId}/getComment")
    public ResponseEntity<StateResponse<Object>> getFormComment(@PathVariable String formId, Pageable pageable) {
        return ResponseEntity.ok(getFormDetailUseCase.execute(formId, pageable));
    }

    @PostMapping("/newTopic")
    public ResponseEntity<StateResponse<Object>> newTopic(@RequestBody @Valid TopicRequest request) {
        return ResponseEntity.ok(createTopicUseCase.execute(request));
    }

    @GetMapping("/topic")
    public ResponseEntity<StateResponse<Object>> getAllTopic(Pageable pageable) {
        return ResponseEntity.ok(listTopicsUseCase.execute(pageable));
    }

    @GetMapping("/topics")
    public ResponseEntity<StateResponse<Object>> getAllTopics(Pageable pageable) {
        return ResponseEntity.ok(listTopicsUseCase.executeWithoutForms(pageable));
    }

    @GetMapping("/{topicId}")
    public ResponseEntity<StateResponse<Object>> getAllFormFromTopic(
            @PathVariable Long topicId,
            @RequestParam(required = false) String tag,
            Pageable pageable) {
        return ResponseEntity.ok(listFormsUseCase.executeForTopic(topicId, tag, pageable));
    }

    @GetMapping("/topic/{topicId}/tags")
    public ResponseEntity<StateResponse<Object>> getTagsByTopic(@PathVariable Long topicId) {
        return ResponseEntity.ok(getTopicTagsUseCase.execute(topicId));
    }

    @DeleteMapping("/{formId}/delete")
    public ResponseEntity<StateResponse<Object>> deleteForm(@PathVariable String formId) {
        return ResponseEntity.ok(deleteFormUseCase.execute(formId));
    }

    @GetMapping("/{formId}/getVote")
    public ResponseEntity<StateResponse<Object>> getVote(@PathVariable String formId) {
        return ResponseEntity.ok(getFormVoteUseCase.execute(formId));
    }

    @GetMapping("/search")
    public ResponseEntity<StateResponse<Object>> searchForm(@RequestParam String keyword, Pageable pageable) {
        return ResponseEntity.ok(listFormsUseCase.searchByKeyword(keyword, pageable));
    }

    @PostMapping("/session/start")
    public ResponseEntity<StateResponse<Object>> startSession() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String sessionId = manageFormSessionUseCase.startSession(username);
        return ResponseEntity.ok(StateResponse.builder().result(sessionId).build());
    }

    @PostMapping("/session/{sessionId}/discard")
    public ResponseEntity<StateResponse<Object>> discardSession(@PathVariable String sessionId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        manageFormSessionUseCase.discardSession(sessionId, username);
        return ResponseEntity.ok(StateResponse.builder().message("Session discarded").build());
    }


    @GetMapping("/session/{sessionId}/questions")
    public ResponseEntity<StateResponse<Object>> getSessionQuestions(@PathVariable String sessionId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        FormSession session = manageFormSessionUseCase.getSession(sessionId, username);
        if (session == null) {
			return ResponseEntity.notFound().build();
		}
        return ResponseEntity.ok(StateResponse.builder().result(session.getQuestions()).build());
    }

    @PostMapping("/session/{sessionId}/questions")
    public ResponseEntity<StateResponse<Object>> updateSessionQuestions(
            @PathVariable String sessionId,
            @RequestBody java.util.List<Question> questions) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        manageFormSessionUseCase.updateQuestions(sessionId, username, questions);
        return ResponseEntity.ok(StateResponse.builder().message("Questions updated successfully").build());
    }
}
