package com.example.demo.modules.document.upload.application.usecase.command;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import com.example.demo.modules.document.upload.application.command.UpdatePdfCommand;
import com.example.demo.modules.document.upload.application.dto.PdfFileDto;
import com.example.demo.modules.document.upload.application.mapper.PdfFileMapper;
import com.example.demo.modules.document.upload.application.port.output.DocumentPersistencePort;

import com.example.demo.modules.document.shared.domain.model.PdfFile;
import com.example.demo.modules.document.shared.domain.model.Major;
import com.example.demo.modules.document.shared.infrastructure.persistence.MajorRepository;

import lombok.RequiredArgsConstructor;

// TODO FIXME: Đổi tên thành UpdateDocumentUseCase
@Service
@RequiredArgsConstructor
public class UpdatePdfUseCase {

    private final DocumentPersistencePort documentPersistencePort;
    private final MajorRepository majorRepository;
    private final PdfFileMapper pdfFileMapper;

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public PdfFileDto execute(UpdatePdfCommand command) {
        PdfFile pdfFile = documentPersistencePort.findById(command.getId())
                .orElseThrow(() -> new RuntimeException("PdfFile not found with id: " + command.getId()));
        
        if (command.getRequest().getTitle() != null) {
            pdfFile.setTitle(command.getRequest().getTitle());
        }
        if (command.getRequest().getMajorId() != null) {
            Major major = majorRepository.findById(command.getRequest().getMajorId())
                    .orElseThrow(() -> new RuntimeException("Major not found with id: " + command.getRequest().getMajorId()));
            pdfFile.setMajor(major);
        }
        if (command.getRequest().getFileType() != null) {
            pdfFile.setFileType(command.getRequest().getFileType());
        }
        if (command.getRequest().getAuthor() != null) {
            pdfFile.setAuthor(command.getRequest().getAuthor());
        }
        
        PdfFile saved = documentPersistencePort.save(pdfFile);
        return pdfFileMapper.toDto(saved);
    }
}
