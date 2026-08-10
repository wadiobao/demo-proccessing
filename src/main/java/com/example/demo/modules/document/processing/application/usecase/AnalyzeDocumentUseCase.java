package com.example.demo.modules.document.processing.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.modules.document.processing.application.port.output.TokenizerPort;
import com.example.demo.modules.document.processing.application.port.output.KeywordExtractionPort;
import com.example.demo.modules.document.processing.application.port.output.ExtractiveSummaryPort;
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

    private final TokenizerPort tokenizerPort;
    private final KeywordExtractionPort keywordExtractionPort;
    private final ExtractiveSummaryPort extractiveSummaryPort;

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
            List<String> tokens = tokenizerPort.tokenize(text);
            List<String> keywords = keywordExtractionPort.extractKeywords(tokens, 15);
            List<String> sentences = List.of(text.split("(?<=[.!?])\\s+"));
            List<String> summarySentences = extractiveSummaryPort.summarize(sentences, 2);
            String summary = String.join(" ", summarySentences);

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
