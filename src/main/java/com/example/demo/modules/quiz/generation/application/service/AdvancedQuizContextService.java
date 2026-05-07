package com.example.demo.modules.quiz.generation.application.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.example.demo.modules.document.metadata.api.DocumentMetadataFacade;
import com.example.demo.modules.document.metadata.domain.model.DocumentMetadata;
import com.example.demo.modules.quiz.graph.infrastructure.persistence.repository.TagRelationRepository;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for providing advanced context for quiz generation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdvancedQuizContextService {

    private final TagRelationRepository tagRelationRepository;
    private final DocumentMetadataFacade documentMetadataFacade;

    @Data
    @Builder
    public static class CrossContextResult {
        private String targetTag;
        private String snippetB;
        private String sourceTopic; // Chủ đề lớn (Toán, Kinh tế...)
        private boolean isCrossTopic; // Đánh dấu nếu lấy kiến thức từ chủ đề khác
        private boolean isZeroShotFallback;
    }

    public CrossContextResult retrieveRelatedContext(String sourceId, List<String> sourceTags, String currentTopic) {
        if (sourceTags == null || sourceTags.isEmpty()) {
            return fallbackStrategy("No source tags", currentTopic);
        }

        List<String> normalizedTags = sourceTags.stream()
                .map(String::toLowerCase)
                .map(String::trim)
                .toList();

        // 1. Dò Graph lấy Top 3 Tags liên quan nhất
        List<String> relatedTags = tagRelationRepository.findMostRelatedTagsExcludingInput(
                normalizedTags, PageRequest.of(0, 3));

        if (relatedTags == null || relatedTags.isEmpty()) {
            return fallbackStrategy("No related tags found", currentTopic);
        }

        // 2. Tìm kiếm tài liệu bổ trợ
        for (String targetTag : relatedTags) {
            DocumentMetadata fileB = documentMetadataFacade.findByTag(targetTag, sourceId);

            if (fileB != null && fileB.getContent() != null) {
                // Trích xuất snippet sạch (cắt theo câu hoàn chỉnh)
                String snippet = extractSmartSnippet(fileB.getContent(), targetTag);
                
                if (snippet != null && !snippet.isEmpty()) {
                    boolean crossTopic = !currentTopic.equalsIgnoreCase(fileB.getTopic());
                    
                    log.info("Found context via tag '{}'. Source Topic: {}, Found Topic: {}", 
                             targetTag, currentTopic, fileB.getTopic());

                    return CrossContextResult.builder()
                            .targetTag(targetTag)
                            .snippetB(snippet)
                            .sourceTopic(fileB.getTopic())
                            .isCrossTopic(crossTopic)
                            .isZeroShotFallback(false)
                            .build();
                }
            }
        }

        return fallbackStrategy("No content found for related tags", currentTopic);
    }

    private String extractSmartSnippet(String fullText, String keyword) {
        if (fullText == null || fullText.isEmpty()) {
			return null;
		}
        if (fullText.length() < 1000) {
			return fullText.trim();
		}

        String lowerText = fullText.toLowerCase();
        int index = lowerText.indexOf(keyword.toLowerCase());

        // Nếu tag có trong metadata nhưng không tìm thấy trong content (do lỗi OCR hoặc viết tắt)
        if (index == -1) {
            return alignToSentences(fullText.substring(0, Math.min(fullText.length(), 1000)));
        }

        // Lấy phạm vi rộng hơn để đảm bảo đủ ngữ cảnh cho các mức Bloom cao
        int start = Math.max(0, index - 400);
        int end = Math.min(fullText.length(), index + 600);

        return alignToSentences(fullText.substring(start, end));
    }

    private String alignToSentences(String snippet) {
        // Tìm dấu chấm đầu tiên sau điểm bắt đầu để tránh câu bị cụt đầu
        int firstDot = snippet.indexOf(".");
        // Tìm dấu chấm cuối cùng để tránh câu bị cụt đuôi
        int lastDot = snippet.lastIndexOf(".");

        if (firstDot != -1 && lastDot != -1 && lastDot > firstDot) {
            return snippet.substring(firstDot + 1, lastDot + 1).trim();
        }
        return snippet.trim();
    }

    private CrossContextResult fallbackStrategy(String reason, String topic) {
        log.warn("Fallback triggered: {}", reason);
        return CrossContextResult.builder()
                .snippetB("NONE - FORCE_AI_EXTRAPOLATION")
                .sourceTopic(topic)
                .isCrossTopic(false)
                .isZeroShotFallback(true)
                .build();
    }
}
