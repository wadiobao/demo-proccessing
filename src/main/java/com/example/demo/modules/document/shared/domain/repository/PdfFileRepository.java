package com.example.demo.modules.document.shared.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.modules.document.shared.domain.model.Major;
import com.example.demo.modules.document.shared.domain.model.PdfFile;

@Repository
public interface PdfFileRepository extends JpaRepository<PdfFile, Long> {
    boolean existsByCloudinaryId(String cloudinaryId);

    Optional<PdfFile> findByCloudinaryId(String publicId);

    List<PdfFile> findAllByCloudinaryIdIn(List<String> publicIds);

    Page<PdfFile> findAllByMajor(Major major, Pageable pageable);

    Page<PdfFile> findAllByMajorId(Long majorId, Pageable pageable);
}
