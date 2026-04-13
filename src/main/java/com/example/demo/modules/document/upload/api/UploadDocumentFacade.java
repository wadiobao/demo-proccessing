package com.example.demo.modules.document.upload.api;

import org.springframework.stereotype.Service;

import com.example.demo.modules.document.upload.application.dto.PdfFileDto;
import com.example.demo.modules.document.upload.application.query.GetPdfQuery;
import com.example.demo.modules.document.upload.application.usecase.query.GetPdfUseCase;

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

    private final GetPdfUseCase getPdfUseCase;

    // Ví dụ một hàm cho module khác gọi để lấy thông tin File DTO
    public PdfFileDto getDocumentById(Long documentId) {
        GetPdfQuery query = GetPdfQuery.builder().id(documentId).build();
        return getPdfUseCase.execute(query);
    }
}
