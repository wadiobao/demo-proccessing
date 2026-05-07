package com.example.demo.modules.document.annotation.application.usecase.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.modules.document.shared.domain.model.PdfAnnotation;
import com.example.demo.modules.document.shared.domain.model.PdfFile;
import com.example.demo.modules.document.shared.domain.repository.PdfAnnotationRepository;
import com.example.demo.modules.document.shared.domain.repository.PdfFileRepository;
import com.example.demo.modules.identity.domain.model.User;
import com.example.demo.modules.identity.infrastructure.persistence.mapper.IdentityEntityMapper;
import com.example.demo.modules.document.annotation.api.dto.PdfAnnotationRequest;
import com.example.demo.modules.document.annotation.api.dto.PdfAnnotationResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateAnnotationUseCase {

    private final PdfAnnotationRepository annotationRepository;
    private final PdfFileRepository pdfFileRepository;
    private final IdentityEntityMapper identityMapper;

    @Transactional
    public PdfAnnotationResponse execute(User user, PdfAnnotationRequest request) {
        PdfFile pdfFile = pdfFileRepository.findById(request.getPdfId())
                .orElseThrow(() -> new RuntimeException("PdfFile not found: " + request.getPdfId()));

        PdfAnnotation annotation = PdfAnnotation.builder()
                .pdf(pdfFile)
                .user(identityMapper.toEntity(user))
                .type(request.getType())
                .color(request.getColor())
                .page(request.getPage())
                .text(request.getText())
                .comment(request.getComment())
                .rects(request.getRects())
                .build();

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
