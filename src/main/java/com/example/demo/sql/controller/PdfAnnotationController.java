package com.example.demo.sql.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import com.example.demo.sql.dto.PdfAnnotationRequest;
import com.example.demo.sql.service.iservice.IPdfAnnotationService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Controller xử lý các yêu cầu ghi chú PDF (Highlight & Comment).
 * Đảm bảo chỉ người dùng tạo ghi chú mới có quyền chỉnh sửa/xóa.
 */
@RestController
@RequestMapping("/api/v1/annotations")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PdfAnnotationController {

    IPdfAnnotationService annotationService;

    /**
     * Lấy danh sách ghi chú của người dùng hiện tại trên một file PDF cụ thể.
     * @param pdfId ID của tập tin PDF.
     * @return Danh sách ghi chú riêng tư.
     */
    @GetMapping("/pdf/{pdfId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<StateResponse<Object>> getAnnotations(@PathVariable Long pdfId) {
        return ResponseEntity.ok(
                StateResponse.builder()
                        .result(annotationService.getAnnotationsByPdfId(pdfId))
                        .build()
        );
    }

    /**
     * Lưu một ghi chú hoặc highlight mới.
     * @param request Thông tin ghi chú và tọa độ bôi đen.
     * @return Ghi chú đã được lưu.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<StateResponse<Object>> createAnnotation(@RequestBody PdfAnnotationRequest request) {
        return ResponseEntity.ok(
                StateResponse.builder()
                        .result(annotationService.createAnnotation(request))
                        .build()
        );
    }

    /**
     * Cập nhật một ghi chú đã có (đổi màu, sửa chữ).
     * @param id ID của ghi chú.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<StateResponse<Object>> updateAnnotation(
            @PathVariable Long id, 
            @RequestBody PdfAnnotationRequest request) {
        return ResponseEntity.ok(
                StateResponse.builder()
                        .result(annotationService.updateAnnotation(id, request))
                        .build()
        );
    }

    /**
     * Xóa sạch một ghi chú hoặc highlight.
     * @param id ID của ghi chú cần xóa.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<StateResponse<Object>> deleteAnnotation(@PathVariable Long id) {
        annotationService.deleteAnnotation(id);
        return ResponseEntity.ok(
                StateResponse.builder().build()
        );
    }
}
