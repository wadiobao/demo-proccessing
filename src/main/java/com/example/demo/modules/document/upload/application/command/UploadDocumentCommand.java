package com.example.demo.modules.document.upload.application.command;

import org.springframework.web.multipart.MultipartFile;
import com.example.demo.modules.document.upload.api.request.UploadDocumentRequest;

import lombok.Builder;
import lombok.Getter;

// TODO FIXME: Đổi tên thành UploadDocumentCommand
@Getter
@Builder
public class UploadDocumentCommand {
    private final MultipartFile file;
    private final UploadDocumentRequest request;
}
