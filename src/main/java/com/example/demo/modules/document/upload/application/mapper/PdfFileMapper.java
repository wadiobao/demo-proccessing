package com.example.demo.modules.document.upload.application.mapper;

import org.springframework.stereotype.Component;

import com.example.demo.modules.document.shared.domain.model.PdfFile;
import com.example.demo.modules.document.upload.api.response.PdfFileResponse;
import com.example.demo.modules.document.upload.application.dto.PdfFileDto;

// TODO FIXME: Đổi tên thành DocumentMapper
@Component
public class PdfFileMapper {

    public PdfFileDto toDto(PdfFile pdfFile) {
        if (pdfFile == null) {
			return null;
		}
        
        return PdfFileDto.builder()
                .id(pdfFile.getId())
                .title(pdfFile.getTitle())
                .pdfUrl(pdfFile.getPdfUrl())
                .cloudinaryId(pdfFile.getCloudinaryId())
                .majorId(pdfFile.getMajor() != null ? pdfFile.getMajor().getId() : null)
                .majorName(pdfFile.getMajor() != null ? pdfFile.getMajor().getDisplayName() : null)
                .fileType(pdfFile.getFileType())
                .author(pdfFile.getAuthor())
                .build();
    }
    
    public PdfFileResponse toResponse(PdfFileDto dto) {
        if (dto == null) {
			return null;
		}
        return PdfFileResponse.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .pdfUrl(dto.getPdfUrl())
                .cloudinaryId(dto.getCloudinaryId())
                .majorId(dto.getMajorId())
                .majorName(dto.getMajorName())
                .fileType(dto.getFileType())
                .author(dto.getAuthor())
                .build();
    }
}
