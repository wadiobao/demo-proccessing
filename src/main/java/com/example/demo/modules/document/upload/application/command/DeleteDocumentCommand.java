package com.example.demo.modules.document.upload.application.command;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

// TODO FIXME: Đổi tên thành DeleteDocumentCommand
@Getter
@Builder
public class DeleteDocumentCommand {
    private final List<String> cloudinaryIds;
}
