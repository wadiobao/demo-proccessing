package com.example.demo.modules.document.processing.application.port.output;

import java.util.List;

/**
 * Port for interacting with AI models to analyze document content.
 * 
 * <p>
 * Định nghĩa hợp đồng cho việc phân tích văn bản, tóm tắt 
 * hoặc trích xuất thông tin thông minh từ nội dung đã trích xuất.
 */
public interface AiAnalysisPort {

    /**
     * Analyzes text and returns a summary or insights.
     * 
     * @param prompt the instructions for the AI
     * @return the AI generated response
     */
    String analyze(String prompt);

    /**
     * Extracts keywords or tags from the content.
     * 
     * @param content the document text
     * @param count number of keywords requested
     * @return list of keywords
     */
    List<String> extractKeywords(String content, int count);
}
