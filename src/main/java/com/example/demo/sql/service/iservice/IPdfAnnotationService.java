package com.example.demo.sql.service.iservice;

import java.util.List;
import com.example.demo.sql.dto.PdfAnnotationRequest;
import com.example.demo.sql.dto.PdfAnnotationResponse;

/**
 * Interface cho dịch vụ xử lý ghi chú PDF.
 */
public interface IPdfAnnotationService {
    /**
     * Lấy danh sách ghi chú theo PDF ID cho người dùng hiện tại.
     */
    List<PdfAnnotationResponse> getAnnotationsByPdfId(Long pdfId);

    /**
     * Tạo mới một ghi chú.
     */
    PdfAnnotationResponse createAnnotation(PdfAnnotationRequest request);

    /**
     * Cập nhật ghi chú đã có.
     */
    PdfAnnotationResponse updateAnnotation(Long id, PdfAnnotationRequest request);

    /**
     * Xóa ghi chú.
     */
    void deleteAnnotation(Long id);
}
