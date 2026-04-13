package com.example.demo.modules.document.upload.application.query;

import lombok.Builder;
import lombok.Getter;

// TODO FIXME: Đổi tên thành ListDocumentQuery
@Getter
@Builder
public class ListPdfQuery {
    private final int page;
    private final int size;
}
