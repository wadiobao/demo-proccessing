package com.example.demo.modules.document.upload.api;

import org.springframework.stereotype.Service;

import com.example.demo.modules.document.upload.application.dto.DocumentDto;
import com.example.demo.modules.document.upload.application.query.GetDocumentQuery;
import com.example.demo.modules.document.upload.application.usecase.query.GetDocumentUseCase;

import lombok.RequiredArgsConstructor;

/**
 * Cổng giao tiếp nội bộ (Internal API).
 * Dùng để các Module khác (như processing, retrieval, quiz) gọi vào
 * nhánh Upload mà không phụ thuộc trực tiếp vào UseCase hay Controller.
 */
// TODO FIXME: Đổi tên thành DocumentUploadFacade
@Service
@RequiredArgsConstructor
public class UploadDocumentFacade {

    private final GetDocumentUseCase getPdfUseCase;

    // Ví dụ một hàm cho module khác gọi để lấy thông tin File DTO
    public DocumentDto getDocumentById(Long documentId) {
        GetDocumentQuery query = GetDocumentQuery.builder().id(documentId).build();
        return getPdfUseCase.execute(query);
    }
}
