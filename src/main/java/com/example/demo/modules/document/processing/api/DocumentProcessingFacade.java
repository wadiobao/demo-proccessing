package com.example.demo.modules.document.processing.api;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.modules.document.processing.application.usecase.AnalyzeDocumentUseCase;
import com.example.demo.modules.document.processing.application.usecase.ExtractTextUseCase;
import com.example.demo.modules.document.processing.domain.model.ExtractedContent;

import lombok.RequiredArgsConstructor;

/**
 * Public Facade for the Document Processing module.
 * 
 * <p>
 * Đây là điểm truy cập chính cho các module khác (ví dụ: quiz, search) 
 * để thực hiện xử lý hoặc phân tích tài liệu mà không cần biết chi tiết bên trong.
 */
@Component
@RequiredArgsConstructor
public class DocumentProcessingFacade {

    private final ExtractTextUseCase extractTextUseCase;
    private final AnalyzeDocumentUseCase analyzeDocumentUseCase;

    /**
     * Coordinate full document processing: text extraction + AI analysis.
     * 
     * @param file the document to process
     * @return structured and analyzed content
     */
    public ExtractedContent processDocument(MultipartFile file) {
        String rawText = extractTextUseCase.execute(file);
        String fileName = file.getOriginalFilename();
        String extension = getExtension(fileName);
        
        return analyzeDocumentUseCase.execute(rawText, extension.toUpperCase());
    }

    /**
     * Directly analyze raw text without extraction.
     * 
     * @param text raw text
     * @return analyzed content
     */
    public ExtractedContent analyzeText(String text) {
        return analyzeDocumentUseCase.execute(text, "RAW_TEXT");
    }

    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "UNKNOWN";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
}
