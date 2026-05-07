package com.example.demo.modules.document.annotation.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.document.annotation.application.usecase.command.CreateAnnotationUseCase;
import com.example.demo.modules.document.annotation.application.usecase.command.DeleteAnnotationUseCase;
import com.example.demo.modules.document.annotation.application.usecase.command.UpdateAnnotationUseCase;
import com.example.demo.modules.document.annotation.application.usecase.query.RetrieveAnnotationsUseCase;
import com.example.demo.modules.identity.domain.model.User;
import com.example.demo.modules.identity.domain.repository.IUserRepository;
import com.example.demo.modules.document.annotation.api.dto.PdfAnnotationRequest;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/v1/annotations")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AnnotationController {

    CreateAnnotationUseCase createAnnotationUseCase;
    UpdateAnnotationUseCase updateAnnotationUseCase;
    DeleteAnnotationUseCase deleteAnnotationUseCase;
    RetrieveAnnotationsUseCase retrieveAnnotationsUseCase;
    IUserRepository userRepository;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    @GetMapping("/pdf/{pdfId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<StateResponse<Object>> getAnnotations(@PathVariable Long pdfId) {
        User user = getCurrentUser();
        return ResponseEntity.ok(
                StateResponse.builder()
                        .result(retrieveAnnotationsUseCase.execute(pdfId, user))
                        .build()
        );
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<StateResponse<Object>> createAnnotation(@RequestBody PdfAnnotationRequest request) {
        User user = getCurrentUser();
        return ResponseEntity.ok(
                StateResponse.builder()
                        .result(createAnnotationUseCase.execute(user, request))
                        .build()
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<StateResponse<Object>> updateAnnotation(
            @PathVariable Long id, 
            @RequestBody PdfAnnotationRequest request) {
        User user = getCurrentUser();
        return ResponseEntity.ok(
                StateResponse.builder()
                        .result(updateAnnotationUseCase.execute(id, user, request))
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<StateResponse<Object>> deleteAnnotation(@PathVariable Long id) {
        User user = getCurrentUser();
        deleteAnnotationUseCase.execute(id, user);
        return ResponseEntity.ok(
                StateResponse.builder().build()
        );
    }
}
