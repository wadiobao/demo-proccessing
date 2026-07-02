package com.example.demo.modules.document.upload.application.command;

import org.springframework.web.multipart.MultipartFile;
import com.example.demo.modules.document.upload.api.request.PdfFileRequest;

import lombok.Builder;
import lombok.Getter;

// TODO FIXME: Đổi tên thành UploadDocumentCommand
@Getter
@Builder
public class UploadPdfCommand {
    private final MultipartFile file;
    private final PdfFileRequest request;
}
