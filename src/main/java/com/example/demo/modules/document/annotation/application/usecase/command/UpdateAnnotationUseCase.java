package com.example.demo.modules.document.annotation.application.usecase.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.modules.document.shared.domain.model.PdfAnnotation;
import com.example.demo.modules.document.shared.domain.repository.PdfAnnotationRepository;
import com.example.demo.modules.identity.domain.model.User;
import com.example.demo.modules.document.annotation.api.dto.PdfAnnotationRequest;
import com.example.demo.modules.document.annotation.api.dto.PdfAnnotationResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateAnnotationUseCase {

    private final PdfAnnotationRepository annotationRepository;

    @Transactional
    public PdfAnnotationResponse execute(Long id, User user, PdfAnnotationRequest request) {
        PdfAnnotation annotation = annotationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Annotation not found: " + id));

        // Kiểm tra quyền sở hữu
        if (!annotation.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied: You don't own this annotation.");
        }

        annotation.setColor(request.getColor());
        annotation.setText(request.getText());
        annotation.setComment(request.getComment());
        annotation.setRects(request.getRects());

        annotation = annotationRepository.save(annotation);

        return PdfAnnotationResponse.builder()
                .id(annotation.getId())
                .pdfId(annotation.getPdf().getId())
                .type(annotation.getType())
                .color(annotation.getColor())
                .page(annotation.getPage())
                .text(annotation.getText())
                .comment(annotation.getComment())
                .rects(annotation.getRects())
                .createdAt(annotation.getCreatedAt())
                .build();
    }
}
