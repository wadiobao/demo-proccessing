package com.example.demo.modules.document.progress.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.document.progress.application.usecase.command.SaveProgressUseCase;
import com.example.demo.modules.document.progress.application.usecase.query.GetProgressUseCase;
import com.example.demo.modules.identity.domain.model.User;
import com.example.demo.modules.identity.domain.repository.IUserRepository;
import com.example.demo.modules.document.progress.api.dto.ReadingProgressRequest;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/v1/reading-progress")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProgressController {

    SaveProgressUseCase saveProgressUseCase;
    GetProgressUseCase getProgressUseCase;
    IUserRepository userRepository;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<StateResponse<Object>> saveProgress(@RequestBody ReadingProgressRequest request) {
        User user = getCurrentUser();
        return ResponseEntity.ok(
                StateResponse.builder()
                        .result(saveProgressUseCase.execute(user.getId(), request))
                        .build()
        );
    }

    @GetMapping("/{pdfId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<StateResponse<Object>> getProgress(@PathVariable Long pdfId) {
        User user = getCurrentUser();
        return ResponseEntity.ok(
                StateResponse.builder()
                        .result(getProgressUseCase.execute(user.getId(), pdfId))
                        .build()
        );
    }
}
