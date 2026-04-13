package com.example.demo.modules.document.processing.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.modules.document.processing.application.port.output.TextExtractorPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Use case for extracting plain text from a document.
 * 
 * <p>
 * Phối hợp với Registry để tìm bộ trích xuất phù hợp và thực hiện
 * lấy nội dung văn bản từ tệp tin.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExtractTextUseCase {

    private final DocumentProcessingRegistry registry;

    /**
     * Extracts text from the given file based on its extension.
     * 
     * @param file the multipart file to process
     * @return the extracted text
     * @throws RuntimeException if no supported extractor is found or processing fails
     */
    public String execute(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        String extension = getExtension(fileName);

        TextExtractorPort extractor = registry.getExtractor(extension)
                .orElseThrow(() -> new RuntimeException("Định dạng file không được hỗ trợ xử lý: " + extension));

        try {
            return extractor.extractText(file);
        } catch (Exception e) {
            log.error("Failed to extract text from file {}: {}", fileName, e.getMessage());
            throw new RuntimeException("Lỗi trong quá trình trích xuất văn bản: " + e.getMessage());
        }
    }

    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
}
