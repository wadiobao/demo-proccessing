package com.example.demo.modules.document.upload.api.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

// TODO FIXME: Đổi tên thành ListDocumentQueryRequest
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ListDocumentQueryRequest {
    Long majorId;
    int size;
    int numPage;
}
