package com.example.demo.modules.community.reputation.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.community.forum.infrastructure.persistence.entity.Form;
import com.example.demo.modules.community.forum.infrastructure.persistence.repository.FormRepository;
import com.example.demo.modules.community.reputation.application.usecase.command.CastVoteUseCase;
import com.example.demo.modules.identity.domain.model.User;
import com.example.demo.modules.identity.domain.repository.IUserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/vote")
@RequiredArgsConstructor
public class VoteController {

    private final CastVoteUseCase castVoteUseCase;
    private final IUserRepository userRepository;
    private final FormRepository formRepository;

    @PostMapping("/{formId}")
    public ResponseEntity<StateResponse<Object>> castVote(
            @PathVariable String formId,
            @RequestParam int value) {
        
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Form form = formRepository.findById(formId)
                .orElseThrow(() -> new RuntimeException("Form not found"));

        castVoteUseCase.execute(user, form, value);

        return ResponseEntity.ok(StateResponse.builder().message("Vote recorded successfully").build());
    }
}
