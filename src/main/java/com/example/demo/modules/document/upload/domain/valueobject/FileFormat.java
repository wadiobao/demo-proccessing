package com.example.demo.modules.document.upload.domain.valueobject;

import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Value Object đại diện cho định dạng (loại MIME) của file.
 * Chỉ cho phép khởi tạo nếu định dạng nằm trong danh sách WhiteList của Doanh nghiệp.
 */
@Getter
@EqualsAndHashCode
public class FileFormat {
    
    private final String mimeType;
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of("application/pdf");
    
    public FileFormat(String mimeType) {
        if (mimeType == null || mimeType.trim().isEmpty()) {
            throw new IllegalArgumentException("Loại file không thể rỗng.");
        }
        if (!ALLOWED_MIME_TYPES.contains(mimeType.toLowerCase())) {
            throw new IllegalArgumentException("Định dạng file không được hỗ trợ: " + mimeType);
        }
        this.mimeType = mimeType.toLowerCase();
    }
}
