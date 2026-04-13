package com.example.demo.mongo.service.quiz;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.example.demo.mongo.entity.Content;
import com.example.demo.sql.repository.TagRelationRepository;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdvancedQuizContextService {

    private final TagRelationRepository tagRelationRepository;
    private final MongoTemplate mongoTemplate;

    @Data
    @Builder
    public static class CrossContextResult {
        private String targetTag;
        private String snippetB;
        private boolean isZeroShotFallback;
    }

    /**
     * Finds related context from other files by traversing the Tag Graph.
     * 
     * @param sourceId the ID of the source document
     * @param sourceTags the tags extracted from the source document
     * @return the CrossContextResult containing the extracted snippet or fallback flag
     */
    public CrossContextResult retrieveRelatedContext(String sourceId, List<String> sourceTags) {
        if (sourceTags == null || sourceTags.isEmpty()) {
            return fallbackStrategy("No source tags available");
        }

        List<String> normalizedTags = sourceTags.stream()
                .map(String::toLowerCase)
                .map(String::trim)
                .toList();

        // 1. Dò Graph lấy Tag đối diện (Tag B) có điểm cao nhất
        List<String> relatedTags = tagRelationRepository.findMostRelatedTagsExcludingInput(normalizedTags, PageRequest.of(0, 1));
        
        if (relatedTags == null || relatedTags.isEmpty()) {
            return fallbackStrategy("No related tags found in Tag Graph (Cold Start)");
        }
        
        String targetTag = relatedTags.get(0);
        log.info("Found related tag: '{}' for source tags: {}", targetTag, normalizedTags);

        // 2. Lấy ngẫu nhiên (hoặc top 1) File B từ MongoDB chứa thẻ Tag B này (loại trừ File A)
        Query query = new Query();
        query.addCriteria(Criteria.where("id").ne(sourceId));
        // Use regex for case-insensitive exact tag match or just exact match if we save them normalized
        query.addCriteria(Criteria.where("tags").regex("^" + targetTag + "$", "i"));
        query.limit(1);

        Content fileB = mongoTemplate.findOne(query, Content.class);

        if (fileB == null || fileB.getContent() == null) {
            return fallbackStrategy("No file found containing the related tag: " + targetTag);
        }

        // 3. Xén (Chunk) File B lấy Snippet chứa targetTag
        String snippet = extractSnippet(fileB.getContent(), targetTag);

        return CrossContextResult.builder()
                .targetTag(targetTag)
                .snippetB(snippet)
                .isZeroShotFallback(false)
                .build();
    }

    private CrossContextResult fallbackStrategy(String reason) {
        log.warn("Fallback triggered: {}", reason);
        return CrossContextResult.builder()
                .snippetB("NONE - FORCE_AI_EXTRAPOLATION")
                .isZeroShotFallback(true)
                .build();
    }

    /**
     * Tìm vị trí từ khoá trong văn bản và cắt lấy 150 - 200 từ xung quanh.
     */
    private String extractSnippet(String fullText, String keyword) {
        if (fullText.length() < 1000) {
            return fullText; // If too short, just return whole file
        }
        
        int index = fullText.toLowerCase().indexOf(keyword.toLowerCase());
        if (index == -1) {
            return fullText.substring(0, Math.min(fullText.length(), 1000));
        }

        int start = Math.max(0, index - 300);
        int end = Math.min(fullText.length(), index + 600);
        
        return fullText.substring(start, end).trim();
    }
}
