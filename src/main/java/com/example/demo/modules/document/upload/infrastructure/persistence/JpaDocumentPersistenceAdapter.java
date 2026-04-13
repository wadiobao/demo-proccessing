package com.example.demo.modules.document.upload.infrastructure.persistence;

import com.example.demo.modules.document.shared.domain.model.PdfFile;
import com.example.demo.modules.document.shared.domain.repository.PdfFileRepository;
import com.example.demo.modules.document.upload.application.port.output.DocumentPersistencePort;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * JPA Adapter implement DocumentPersistencePort.
 * Delegate toàn bộ sang Spring Data JPA Repository của tầng shared.
 */
@Component
@RequiredArgsConstructor
public class JpaDocumentPersistenceAdapter implements DocumentPersistencePort {

    // PdfFileRepository là JpaRepository nằm ở shared — bị bọc ở đây, không lộ ra UseCase
    private final PdfFileRepository pdfFileRepository;

    @Override
    public PdfFile save(PdfFile pdfFile) {
        return pdfFileRepository.save(pdfFile);
    }

    @Override
    public Optional<PdfFile> findById(Long id) {
        return pdfFileRepository.findById(id);
    }

    @Override
    public Page<PdfFile> findAll(Pageable pageable) {
        return pdfFileRepository.findAll(pageable);
    }

    @Override
    public Page<PdfFile> findAllByMajorId(Long majorId, Pageable pageable) {
        return pdfFileRepository.findAllByMajorId(majorId, pageable);
    }

    @Override
    public List<PdfFile> findAllByCloudinaryIdIn(List<String> cloudinaryIds) {
        return pdfFileRepository.findAllByCloudinaryIdIn(cloudinaryIds);
    }

    @Override
    public void deleteAll(List<PdfFile> files) {
        pdfFileRepository.deleteAll(files);
    }
}
