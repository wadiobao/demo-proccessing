package com.example.demo.modules.document.upload.application.command;

import com.example.demo.modules.document.upload.api.request.UploadDocumentRequest;

import lombok.Builder;
import lombok.Getter;

// TODO FIXME: Đổi tên thành UpdateDocumentCommand
@Getter
@Builder
public class UpdateDocumentCommand {
    private final Long id;
    private final UploadDocumentRequest request;
}
