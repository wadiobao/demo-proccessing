package com.example.demo.modules.document.upload.domain.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Value Object đại diện cho kích thước File.
 * Tự chịu trách nhiệm đảm bảo quy tắc nghiệp vụ về dung lượng tải lên.
 */
@Getter
@EqualsAndHashCode
public class FileSize {
    
    private final long bytes;
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    
    public FileSize(long bytes) {
        if (bytes <= 0) {
            throw new IllegalArgumentException("Kích thước file không được nhỏ hơn hoặc bằng 0.");
        }
        if (bytes > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Kích thước file vượt quá giới hạn hệ thống cho phép (50MB).");
        }
        this.bytes = bytes;
    }
    
    public double toMegabytes() {
        return (double) bytes / (1024 * 1024);
    }
}
