package com.example.demo.modules.document.upload.application.mapper;

import org.springframework.stereotype.Component;

import com.example.demo.modules.document.shared.domain.model.PdfFile;
import com.example.demo.modules.document.upload.api.response.DocumentResponse;
import com.example.demo.modules.document.upload.application.dto.DocumentDto;

// TODO FIXME: Đổi tên thành DocumentMapper
@Component
public class DocumentMapper {

    public DocumentDto toDto(PdfFile pdfFile) {
        if (pdfFile == null) {
			return null;
		}
        
        return DocumentDto.builder()
                .id(pdfFile.getId())
                .title(pdfFile.getTitle())
                .pdfUrl(pdfFile.getPdfUrl())
                .cloudinaryId(pdfFile.getCloudinaryId())
                .majorId(pdfFile.getMajor() != null ? pdfFile.getMajor().getId() : null)
                .majorName(pdfFile.getMajor() != null ? pdfFile.getMajor().getDisplayName() : null)
                .fileType(pdfFile.getFileType())
                .author(pdfFile.getAuthor())
                .createdAt(pdfFile.getCreatedAt())
                .build();
    }
    
    public DocumentResponse toResponse(DocumentDto dto) {
        if (dto == null) {
			return null;
		}
        return DocumentResponse.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .pdfUrl(dto.getPdfUrl())
                .cloudinaryId(dto.getCloudinaryId())
                .majorId(dto.getMajorId())
                .majorName(dto.getMajorName())
                .fileType(dto.getFileType())
                .author(dto.getAuthor())
                .createdAt(dto.getCreatedAt())
                .build();
    }
}
