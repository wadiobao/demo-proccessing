package com.example.demo.modules.document.annotation.application.usecase.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.modules.document.shared.domain.model.PdfAnnotation;
import com.example.demo.modules.document.shared.domain.repository.PdfAnnotationRepository;
import com.example.demo.modules.identity.domain.model.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteAnnotationUseCase {

    private final PdfAnnotationRepository annotationRepository;

    @Transactional
    public void execute(Long id, User user) {
        PdfAnnotation annotation = annotationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Annotation not found: " + id));

        // Kiểm tra quyền sở hữu
        if (!annotation.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied: You don't own this annotation.");
        }

        annotationRepository.delete(annotation);
    }
}
