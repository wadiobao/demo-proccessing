package com.example.demo.modules.document.upload.application.usecase.query;

import org.springframework.stereotype.Service;
import com.example.demo.modules.document.upload.application.query.GetPdfQuery;
import com.example.demo.modules.document.upload.application.dto.PdfFileDto;
import com.example.demo.modules.document.upload.application.mapper.PdfFileMapper;
import com.example.demo.modules.document.upload.application.port.output.DocumentPersistencePort;

import com.example.demo.modules.document.shared.domain.model.PdfFile;
import lombok.RequiredArgsConstructor;

// TODO FIXME: Đổi tên thành GetDocumentUseCase
@Service
@RequiredArgsConstructor
public class GetPdfUseCase {

    private final DocumentPersistencePort documentPersistencePort;
    private final PdfFileMapper pdfFileMapper;

    public PdfFileDto execute(GetPdfQuery query) {
        PdfFile pdfFile = documentPersistencePort.findById(query.getId())
                .orElseThrow(() -> new RuntimeException("PdfFile not found with id: " + query.getId()));
        return pdfFileMapper.toDto(pdfFile);
    }
}
