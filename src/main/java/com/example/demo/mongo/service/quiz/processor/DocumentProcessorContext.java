package com.example.demo.mongo.service.quiz.processor;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

/**
 * Factory and Context for selecting the appropriate document processor.
 * 
 * <p>
 * Tự động nhận diện và lựa chọn trình xử lý (Processor) phù hợp dựa trên
 * định dạng file (MIME type) để trích xuất văn bản phục vụ tạo bài tập.
 *
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
public class DocumentProcessorContext {

    private final List<IDocumentProcessor> processors;

    /**
     * Resolves the correct processor implementation for the provided file.
     *
     * @param file source file / tệp tin nguồn
     * @return supported IDocumentProcessor / trình xử lý tương ứng
     * @throws IllegalArgumentException if format is unsupported / lỗi nếu định dạng
     *                                  không được hỗ trợ
     */
    public IDocumentProcessor getProcessor(MultipartFile file) {
        String contentType = file.getContentType();

        // Handle edge case where content type might be null or vague
        if (contentType == null) {
            String filename = file.getOriginalFilename();
            if (filename != null && filename.toLowerCase().endsWith(".txt")) {
                contentType = "text/plain";
            }
        }

        for (IDocumentProcessor processor : processors) {
            if (processor.supports(contentType)) {
                return processor;
            }
        }

        throw new IllegalArgumentException("Định dạng file không được hỗ trợ: " + contentType);
    }
}
