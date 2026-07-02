package com.example.demo.modules.document.upload.application.usecase.query;

import org.springframework.stereotype.Service;
import com.example.demo.modules.document.upload.application.query.GetDocumentQuery;
import com.example.demo.modules.document.upload.application.dto.DocumentDto;
import com.example.demo.modules.document.upload.application.mapper.DocumentMapper;
import com.example.demo.modules.document.upload.application.port.output.DocumentPersistencePort;

import com.example.demo.modules.document.shared.domain.model.PdfFile;
import lombok.RequiredArgsConstructor;

// TODO FIXME: Đổi tên thành GetDocumentUseCase
@Service
@RequiredArgsConstructor
public class GetDocumentUseCase {

    private final DocumentPersistencePort documentPersistencePort;
    private final DocumentMapper pdfFileMapper;

    public DocumentDto execute(GetDocumentQuery query) {
        PdfFile pdfFile = documentPersistencePort.findById(query.getId())
                .orElseThrow(() -> new RuntimeException("PdfFile not found with id: " + query.getId()));
        return pdfFileMapper.toDto(pdfFile);
    }
}
