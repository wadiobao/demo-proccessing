package com.example.demo.modules.document.upload.application.usecase.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.demo.modules.document.shared.domain.model.PdfFile;
import com.example.demo.modules.document.upload.application.port.output.DocumentPersistencePort;
import com.example.demo.modules.document.upload.application.query.ListDocumentQuery;
import com.example.demo.modules.document.upload.application.query.FilterDocumentQuery;
import com.example.demo.modules.document.upload.application.dto.DocumentDto;
import com.example.demo.modules.document.upload.application.mapper.DocumentMapper;

import lombok.RequiredArgsConstructor;

// TODO FIXME: Đổi tên thành ListDocumentUseCase
@Service
@RequiredArgsConstructor
public class ListDocumentUseCase {

    private final DocumentPersistencePort documentPersistencePort;
    private final DocumentMapper pdfFileMapper;

    public Page<DocumentDto> executeAll(ListDocumentQuery query) {
        Page<PdfFile> page = documentPersistencePort.findAll(PageRequest.of(query.getPage(), query.getSize()));
        return page.map(pdfFileMapper::toDto);
    }

    public Page<DocumentDto> executeFilter(FilterDocumentQuery query) {
        Pageable pageable = PageRequest.of(query.getRequest().getNumPage(), query.getRequest().getSize());
        Page<PdfFile> page;
        if (query.getRequest().getMajorId() != null) {
            page = documentPersistencePort.findAllByMajorId(query.getRequest().getMajorId(), pageable);
        } else {
            page = documentPersistencePort.findAll(pageable);
        }
        return page.map(pdfFileMapper::toDto);
    }
}
