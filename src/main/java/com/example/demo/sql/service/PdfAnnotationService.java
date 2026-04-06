package com.example.demo.sql.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.sql.dto.PdfAnnotationRequest;
import com.example.demo.sql.dto.PdfAnnotationResponse;
import com.example.demo.sql.entity.PdfAnnotation;
import com.example.demo.sql.entity.PdfFile;
import com.example.demo.sql.entity.User;
import com.example.demo.sql.repository.PdfAnnotationRepository;
import com.example.demo.sql.repository.PdfFileRepository;
import com.example.demo.sql.repository.UserRepository;
import com.example.demo.sql.service.iservice.IPdfAnnotationService;

import lombok.RequiredArgsConstructor;

/**
 * Triển khai dịch vụ xử lý ghi chú PDF.
 */
@Service
@RequiredArgsConstructor
public class PdfAnnotationService implements IPdfAnnotationService {

    private final PdfAnnotationRepository annotationRepository;
    private final PdfFileRepository pdfFileRepository;
    private final UserRepository userRepository;

    /**
     * Lấy người dùng hiện tại dựa trên JWT Token.
     */
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    @Override
    public List<PdfAnnotationResponse> getAnnotationsByPdfId(Long pdfId) {
        User user = getCurrentUser();
        return annotationRepository.findAllByPdfIdAndUserId(pdfId, user.getId()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PdfAnnotationResponse createAnnotation(PdfAnnotationRequest request) {
        User user = getCurrentUser();
        PdfFile pdfFile = pdfFileRepository.findById(request.getPdfId())
                .orElseThrow(() -> new RuntimeException("PdfFile not found: " + request.getPdfId()));

        PdfAnnotation annotation = PdfAnnotation.builder()
                .pdf(pdfFile)
                .user(user)
                .type(request.getType())
                .color(request.getColor())
                .page(request.getPage())
                .text(request.getText())
                .comment(request.getComment())
                .rects(request.getRects())
                .build();

        return toResponse(annotationRepository.save(annotation));
    }

    @Override
    @Transactional
    public PdfAnnotationResponse updateAnnotation(Long id, PdfAnnotationRequest request) {
        User user = getCurrentUser();
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

        return toResponse(annotationRepository.save(annotation));
    }

    @Override
    @Transactional
    public void deleteAnnotation(Long id) {
        User user = getCurrentUser();
        PdfAnnotation annotation = annotationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Annotation not found: " + id));

        // Kiểm tra quyền sở hữu
        if (!annotation.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied: You don't own this annotation.");
        }

        annotationRepository.delete(annotation);
    }

    /**
     * Ánh xạ từ Entity sang DTO.
     */
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
