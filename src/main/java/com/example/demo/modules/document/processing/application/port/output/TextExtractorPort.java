package com.example.demo.modules.document.processing.application.port.output;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

/**
 * Port for extracting text from various document formats.
 * 
 * <p>
 * Định nghĩa hợp đồng cho việc trích xuất nội dung văn bản thô
 * từ các tệp tin tải lên (PDF, Docx, etc.).
 */
public interface TextExtractorPort {

    /**
     * Extracts text from the given file.
     * 
     * @param file the multipart file to process
     * @return the extracted plain text
     * @throws IOException if an error occurs during extraction
     */
    String extractText(MultipartFile file) throws IOException;

    /**
     * Checks if this extractor supports the given file format.
     * 
     * @param extension file extension (e.g., "pdf", "docx")
     * @return true if supported
     */
    boolean supports(String extension);
}
