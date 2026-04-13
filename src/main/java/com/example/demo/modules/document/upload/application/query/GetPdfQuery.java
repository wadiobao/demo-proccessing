package com.example.demo.modules.document.upload.application.query;

import lombok.Builder;
import lombok.Getter;

// TODO FIXME: Đổi tên thành GetDocumentQuery
@Getter
@Builder
public class GetPdfQuery {
    private final Long id;
}
