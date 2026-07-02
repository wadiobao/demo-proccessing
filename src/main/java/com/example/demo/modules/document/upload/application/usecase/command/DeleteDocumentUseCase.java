package com.example.demo.modules.document.upload.application.usecase.command;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import com.example.demo.modules.document.upload.application.command.DeleteDocumentCommand;
import com.example.demo.modules.document.shared.domain.model.PdfFile;
import com.example.demo.modules.document.upload.application.port.output.DocumentPersistencePort;
import com.example.demo.modules.document.upload.application.port.output.FileStoragePort;

import lombok.RequiredArgsConstructor;

// TODO FIXME: Đổi tên thành DeleteDocumentUseCase
@Service
@RequiredArgsConstructor
public class DeleteDocumentUseCase {

    private final DocumentPersistencePort documentPersistencePort;
    private final FileStoragePort fileStoragePort;

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public boolean execute(DeleteDocumentCommand command) {
        try {
            List<PdfFile> files = documentPersistencePort.findAllByCloudinaryIdIn(command.getCloudinaryIds());
            fileStoragePort.deleteFiles(command.getCloudinaryIds());
            documentPersistencePort.deleteAll(files);
            return true;
        } catch (Exception e) {
            // Log error
            return false;
        }
    }
}
