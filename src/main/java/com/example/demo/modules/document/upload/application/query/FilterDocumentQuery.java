package com.example.demo.modules.document.upload.application.query;

import com.example.demo.modules.document.upload.api.request.ListDocumentQueryRequest;
import lombok.Builder;
import lombok.Getter;

// TODO FIXME: Đổi tên thành FilterDocumentQuery
@Getter
@Builder
public class FilterDocumentQuery {
    private final ListDocumentQueryRequest request;
}
