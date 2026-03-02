package com.example.demo.mongo.service.quiz.processor;

import org.springframework.web.multipart.MultipartFile;

/**
 * Strategy interface for multi-format document text extraction.
 * 
 * <p>
 * Định nghĩa các tiêu chuẩn chung cho việc trích xuất nội dung văn bản
 * từ các định dạng tệp khác nhau (Strategy Pattern).
 *
 * @since 1.0
 */
public interface IDocumentProcessor {

    /**
     * Checks if this processor supports the given file content type.
     *
     * @param contentType MIME type of the file
     * @return true if supported, false otherwise
     */
    boolean supports(String contentType);

    /**
     * Extracts text content from the file.
     *
     * @param file File to process
     * @return Extracted text
     * @throws Exception if processing fails
     */
    String extractText(MultipartFile file) throws Exception;
}
