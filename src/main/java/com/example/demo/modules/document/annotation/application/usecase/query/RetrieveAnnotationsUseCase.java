package com.example.demo.modules.document.annotation.application.usecase.query;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.modules.document.shared.domain.model.PdfAnnotation;
import com.example.demo.modules.document.shared.domain.repository.PdfAnnotationRepository;
import com.example.demo.modules.identity.domain.model.User;
import com.example.demo.modules.document.annotation.api.dto.PdfAnnotationResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RetrieveAnnotationsUseCase {

    private final PdfAnnotationRepository annotationRepository;

    @Transactional(readOnly = true)
    public List<PdfAnnotationResponse> execute(Long pdfId, User user) {
        return annotationRepository.findAllByPdfIdAndUserId(pdfId, user.getId()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private PdfAnnotationResponse toResponse(PdfAnnotation annotation) {
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
