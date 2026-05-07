package com.example.demo.modules.document.upload.application.usecase.command;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import com.example.demo.modules.document.upload.application.command.UploadPdfCommand;
import com.example.demo.modules.document.upload.application.dto.PdfFileDto;
import com.example.demo.modules.document.upload.application.mapper.PdfFileMapper;
import com.example.demo.modules.document.upload.application.validator.UploadPdfValidator;
import com.example.demo.modules.document.upload.application.port.output.DocumentPersistencePort;
import com.example.demo.modules.document.upload.application.port.output.FileStoragePort;

import com.example.demo.modules.document.shared.domain.model.PdfFile;
import com.example.demo.modules.document.shared.domain.model.Major;
import com.example.demo.modules.document.shared.infrastructure.persistence.MajorRepository;

import lombok.RequiredArgsConstructor;

// TODO FIXME: Đổi tên thành UploadDocumentUseCase
@Service
@RequiredArgsConstructor
public class UploadPdfUseCase {

    private final DocumentPersistencePort documentPersistencePort;
    private final MajorRepository majorRepository;
    private final FileStoragePort fileStoragePort;
    private final PdfFileMapper pdfFileMapper;
    private final UploadPdfValidator uploadPdfValidator;

    @Transactional
    public PdfFileDto execute(UploadPdfCommand command) throws IOException {
        uploadPdfValidator.validate(command);
        
        Map<String, String> uploadResult = fileStoragePort.uploadFile(command.getFile());
        
        Major major = null;
        if (command.getRequest().getMajorId() != null) {
            major = majorRepository.findById(command.getRequest().getMajorId())
                    .orElseThrow(() -> new RuntimeException("Major not found with id: " + command.getRequest().getMajorId()));
        }

        PdfFile pdfFile = PdfFile.builder()
                .title(command.getRequest().getTitle() != null ? command.getRequest().getTitle() : command.getFile().getOriginalFilename())
                .pdfUrl(uploadResult.get("secure_url"))
                .cloudinaryId(uploadResult.get("public_id"))
                .major(major)
                .fileType(command.getRequest().getFileType())
                .author(command.getRequest().getAuthor())
                .build();
        PdfFile saved = documentPersistencePort.save(pdfFile);
        return pdfFileMapper.toDto(saved);
    }
}
