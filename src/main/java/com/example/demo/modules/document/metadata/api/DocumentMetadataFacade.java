package com.example.demo.modules.document.metadata.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.modules.document.metadata.domain.model.DocumentMetadata;
import com.example.demo.modules.document.metadata.infrastructure.persistence.entity.DocumentMetadataMongoEntity;
import com.example.demo.modules.document.metadata.infrastructure.persistence.repository.DocumentMetadataRepository;
import com.example.demo.modules.document.processing.api.DocumentProcessingFacade;
import com.example.demo.modules.document.processing.domain.model.ExtractedContent;
import com.example.demo.utils.VectorUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Facade for managing document semantic metadata (Topics, tags, embeddings).
 * 
 * <p>
 * Phối hợp việc nhận diện chủ đề thông qua AI và lưu trữ kết quả phân tích
 * tài liệu vào hệ thống Metadata (MongoDB Content collection).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentMetadataFacade {

    private final DocumentMetadataRepository repository;
    private final VectorUtils vectorUtils;
    private final DocumentProcessingFacade documentProcessingFacade;

    /**
     * Saves or retrieves existing metadata for the given content.
     */
    @Transactional
    public DocumentMetadata findOrCreateMetadata(String content, String owner, String originalName) {
        // 1. Check for EXACT match first
        Optional<DocumentMetadataMongoEntity> exactMatch = repository.findFirstByContentAndOwner(content, owner);
        if (exactMatch.isPresent()) {
            DocumentMetadataMongoEntity entity = exactMatch.get();
            // Update originalName if it was missing in the existing record
            if ((entity.getOriginalName() == null || entity.getOriginalName().isBlank()) && originalName != null) {
                entity.setOriginalName(originalName);
                repository.save(entity);
                log.info("Updated missing originalName for existing metadata: {}", originalName);
            }
            log.info("Found an exact match");
            return entity.toDomain();
        }

        log.info("[AI-CALL] Creating text embedding (Vectorization)");
        List<Double> embedding = vectorUtils.createVector(content);
        log.info("[AI-RESULT] Embedding created (Dimensions: {})", embedding != null ? embedding.size() : 0);
        String detectedTopic = null;
        List<String> tags = null;

        // 2. Try Vector Search (Similarity)
        try {
            DocumentMetadata similar = searchSimilar(embedding, 1, owner);
            if (similar != null) {
                if (similar.getVectorSearchScore() >= 0.95) {
                    log.info("Reusing exact metadata for content. Topic: {}", similar.getTopic());
                    
                    // Update originalName in DB if missing
                    repository.findById(similar.getId()).ifPresent(entity -> {
                        if ((entity.getOriginalName() == null || entity.getOriginalName().isBlank()) && originalName != null) {
                            entity.setOriginalName(originalName);
                            repository.save(entity);
                        }
                    });
                    
                    return similar;
                }
                detectedTopic = similar.getTopic();
                tags = similar.getTags();
            }
        } catch (Exception e) {
            log.error("Similarity search failed: {}", e.getMessage());
        }

        // 3. Fallback to AI Analysis if no strong match
        if (detectedTopic == null) {
            try {
                log.info("[AI-CALL] Analyzing text metadata (Topic & Keywords detection)");
                ExtractedContent analyzed = documentProcessingFacade.analyzeText(content);
                tags = analyzed.getKeywords();
                if (tags != null && !tags.isEmpty()) {
                    detectedTopic = analyzed.getSummary() != null ? analyzed.getSummary() : tags.get(0);
                    log.info("[AI-RESULT] Extracted metadata. Topic: {}, Keywords: {}", detectedTopic, tags.size());
                }
            } catch (Exception e) {
                log.error("AI Analysis failed: {}", e.getMessage());
            }
        }

        // 4. Default fallback
        if (detectedTopic == null) {
            detectedTopic = "general:unknown";
            tags = new ArrayList<>();
        }

        DocumentMetadata domain = DocumentMetadata.builder()
                .content(content)
                .owner(owner)
                .embedding(embedding)
                .topic(detectedTopic)
                .tags(tags)
                .originalName(originalName)
                .build();

        DocumentMetadataMongoEntity entity = DocumentMetadataMongoEntity.fromDomain(domain);
        return repository.save(entity).toDomain();
    }

    public DocumentMetadata findById(String id) {
        return repository.findById(id)
                .map(DocumentMetadataMongoEntity::toDomain)
                .orElse(null);
    }

    /**
     * Finds multiple metadata entries by their unique identifiers.
     */
    public List<DocumentMetadata> findByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        List<DocumentMetadata> results = new ArrayList<>();
        repository.findAllById(ids).forEach(entity -> results.add(entity.toDomain()));
        return results;
    }

    /**
     * Searches for visually or semantically similar documents.
     */
    public DocumentMetadata searchSimilar(List<Double> queryVector, int limit, String username) {
        List<DocumentMetadataMongoEntity> results = repository.searchSimilar(queryVector, limit, username);
        if (results == null || results.isEmpty()) {
            return null;
        }

        DocumentMetadataMongoEntity mostSimilar = results.get(0);
        Double score = mostSimilar.getVectorSearchScore();

        if (score != null && score >= 0.7) {
            return mostSimilar.toDomain();
        }

        return null;
    }

    /**
     * Finds a document metadata by tag, excluding a specific ID.
     */
    public DocumentMetadata findByTag(String tag, String excludeId) {
        return repository.findFirstByTagsContainingAndIdNot(tag, excludeId)
                .map(DocumentMetadataMongoEntity::toDomain)
                .orElse(null);
    }
}
