package com.example.demo.sql.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.StateResponse;
import com.example.demo.sql.dto.ReadingProgressRequest;
import com.example.demo.sql.service.iservice.IReadingProgressService;

import lombok.RequiredArgsConstructor;

/**
 * Controller xử lý các API liên quan đến tiến trình đọc sách (Resume Reading).
 */
@RestController
@RequestMapping("/api/v1/progress")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class ReadingProgressController {

    private final IReadingProgressService progressService;

    /**
     * Lưu trữ hoặc cập nhật tiến trình đọc sách vào Redis.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<StateResponse<Object>> saveProgress(@RequestBody ReadingProgressRequest request) {
        progressService.saveProgress(request);
        return ResponseEntity.ok(
                StateResponse.builder()
                        .result("Success: Saved reading progress.")
                        .build()
        );
    }

    /**
     * Lấy tiến trình đọc sách của tài liệu chỉ định cho người dùng hiện tại.
     */
    @GetMapping("/pdf/{pdfId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<StateResponse<Object>> getProgress(@PathVariable Long pdfId) {
        return ResponseEntity.ok(
                StateResponse.builder()
                        .result(progressService.getProgress(pdfId))
                        .build()
        );
    }
}
