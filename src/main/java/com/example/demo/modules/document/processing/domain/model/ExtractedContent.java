package com.example.demo.modules.document.processing.domain.model;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Domain model representing the results of document processing.
 * 
 * <p>
 * Chứa nội dung văn bản đã trích xuất, metadata và các kết quả phân tích sơ bộ.
 */
@Getter
@Builder
@ToString
public class ExtractedContent {
    
    private final String rawText;
    private final String processingStrategy; // e.g., "PDFBOX", "OCR", "POI"
    private final List<String> keywords;
    private final String summary;
    
    /**
     * Checks if the content is empty.
     * 
     * @return true if no text was extracted
     */
    public boolean isEmpty() {
        return rawText == null || rawText.trim().isEmpty();
    }
}
