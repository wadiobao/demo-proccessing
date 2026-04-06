package com.example.demo.sql.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.sql.entity.PdfAnnotation;

/**
 * Repository xử lý các thao tác truy vấn cho ghi chú PDF.
 */
@Repository
public interface PdfAnnotationRepository extends JpaRepository<PdfAnnotation, Long> {
    /**
     * Tìm tất cả ghi chú của một người dùng cụ thể trên một tập tin PDF cụ thể.
     * @param pdfId ID của tập tin PDF.
     * @param userId ID của người dùng.
     * @return Danh sách ghi chú.
     */
    List<PdfAnnotation> findAllByPdfIdAndUserId(Long pdfId, Long userId);
}
