package com.example.demo.sql.service.iservice;

import com.example.demo.sql.dto.ReadingProgressRequest;
import com.example.demo.sql.dto.ReadingProgressResponse;

/**
 * Interface cho dịch vụ quản lý tiến trình đọc sách.
 */
public interface IReadingProgressService {
    /**
     * Lưu trữ hoặc cập nhật tiến trình đọc vào Redis.
     */
    void saveProgress(ReadingProgressRequest request);

    /**
     * Lấy tiến trình đọc từ Redis theo tài liệu.
     */
    ReadingProgressResponse getProgress(Long pdfId);
}
