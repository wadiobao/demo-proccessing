package com.example.demo.modules.document.upload.api.request;

import com.example.demo.enums.FileType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

// TODO FIXME: Đổi tên thành UploadDocumentRequest
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UploadDocumentRequest {
    String title;
    Long majorId;
    FileType fileType;
    String author;
}
