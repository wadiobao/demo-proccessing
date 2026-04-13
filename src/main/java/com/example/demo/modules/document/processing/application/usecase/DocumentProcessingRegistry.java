package com.example.demo.modules.document.processing.application.usecase;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.example.demo.modules.document.processing.application.port.output.TextExtractorPort;

import lombok.RequiredArgsConstructor;

/**
 * Registry and selector for text extraction strategies.
 * 
 * <p>
 * Quản lý danh sách các TextExtractorPort có sẵn và lựa chọn bộ xử lý
 * phù hợp dựa trên phần mở rộng của tệp tin.
 */
@Component
@RequiredArgsConstructor
public class DocumentProcessingRegistry {

    private final List<TextExtractorPort> extractors;

    /**
     * Finds the first extractor that supports the given file extension.
     * 
     * @param extension the file extension (e.g., "pdf")
     * @return an Optional containing the extractor if found
     */
    public Optional<TextExtractorPort> getExtractor(String extension) {
        if (extension == null) {
            return Optional.empty();
        }
        
        String cleanExtension = extension.startsWith(".") ? extension.substring(1) : extension;
        
        return extractors.stream()
                .filter(extractor -> extractor.supports(cleanExtension))
                .findFirst();
    }
}
