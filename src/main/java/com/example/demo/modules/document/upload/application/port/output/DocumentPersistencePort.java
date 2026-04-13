package com.example.demo.modules.document.upload.application.port.output;

import com.example.demo.modules.document.shared.domain.model.PdfFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Output Port: Hợp đồng truy vấn và lưu trữ Document.
 * Cách ly UseCase khỏi JPA/Hibernate cụ thể, cho phép dễ dàng thay DB sau này.
 */
public interface DocumentPersistencePort {
    
    PdfFile save(PdfFile pdfFile);
    
    Optional<PdfFile> findById(Long id);
    
    Page<PdfFile> findAll(Pageable pageable);
    
    Page<PdfFile> findAllByMajorId(Long majorId, Pageable pageable);
    
    List<PdfFile> findAllByCloudinaryIdIn(List<String> cloudinaryIds);
    
    void deleteAll(List<PdfFile> files);
}
