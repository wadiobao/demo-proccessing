package com.example.demo.modules.document.processing.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.modules.document.processing.application.port.output.AiAnalysisPort;
import com.example.demo.modules.document.processing.domain.model.ExtractedContent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Use case for analyzing document content using AI.
 * 
 * <p>
 * Thực hiện trích xuất từ khóa và tạo bản tóm tắt nội dung từ văn bản
 * đã được trích xuất.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyzeDocumentUseCase {

    private final AiAnalysisPort aiAnalysisPort;

    /**
     * Analyzes the provided text and builds an ExtractedContent domain object.
     * 
     * @param text the raw text to analyze
     * @param strategy the strategy used for extraction (for metadata)
     * @return the analyzed content
     */
    public ExtractedContent execute(String text, String strategy) {
        log.info("Analyzing document content with AI strategy: {}", strategy);
        
        try {
            List<String> keywords = aiAnalysisPort.extractKeywords(text, 15);
            String summary = aiAnalysisPort.analyze("Tóm tắt ngắn gọn nội dung sau trong 1-2 câu: " + text);

            return ExtractedContent.builder()
                    .rawText(text)
                    .processingStrategy(strategy)
                    .keywords(keywords)
                    .summary(summary)
                    .build();
        } catch (Exception e) {
            log.warn("AI Analysis failed partially: {}. Returning raw text only.", e.getMessage());
            return ExtractedContent.builder()
                    .rawText(text)
                    .processingStrategy(strategy)
                    .build();
        }
    }
}
