package com.example.demo.modules.document.upload.application.dto;

import java.time.LocalDateTime;

import com.example.demo.enums.FileType;

import lombok.Builder;
import lombok.Data;

// TODO FIXME: Đổi tên thành DocumentDto
@Data
@Builder
public class DocumentDto {
    private Long id;
    private String title;
    private String pdfUrl;
    private String cloudinaryId;
    private Long majorId;
    private String majorName;
    private FileType fileType;
    private String author;
    private LocalDateTime createdAt;
}
