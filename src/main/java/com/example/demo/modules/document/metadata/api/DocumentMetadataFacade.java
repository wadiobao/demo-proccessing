package com.example.demo.modules.document.metadata.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.modules.document.metadata.domain.command.DeleteMetadataCommand;
import com.example.demo.modules.document.metadata.domain.model.DocumentMetadata;
import com.example.demo.modules.document.metadata.infrastructure.persistence.entity.DocumentMetadataMongoEntity;
import com.example.demo.modules.document.metadata.infrastructure.persistence.repository.DocumentMetadataRepository;
import com.example.demo.modules.document.processing.api.DocumentProcessingFacade;
import com.example.demo.modules.document.processing.domain.model.ExtractedContent;
import com.example.demo.modules.document.shared.application.TagNormalizer;
import com.example.demo.modules.document.metadata.application.port.output.EmbeddingPort;
import com.example.demo.modules.document.metadata.application.port.output.VectorIndexPort;
import com.example.demo.modules.document.metadata.application.port.output.DocumentGraphPort;

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
    private final EmbeddingPort embeddingPort;
    private final VectorIndexPort vectorIndexPort;
    private final DocumentGraphPort documentGraphPort;
    private final DocumentProcessingFacade documentProcessingFacade;
    private final TagNormalizer tagNormalizer;
    private final DeleteMetadataCommand deleteMetadataCommand;

    /**
     * Saves or retrieves existing metadata for the given content.
     */
    @Transactional
    public DocumentMetadata findOrCreateMetadata(String content, String owner, String originalName) {
        return findOrCreateMetadata(content, owner, originalName, null);
    }

    /**
     * Saves or retrieves existing metadata for the given content with an optional explicit topic.
     * @deprecated Sử dụng kiến trúc Pipeline Offline thay vì gọi AI bên ngoài.
     */
    @Transactional
    @Deprecated(since = "1.0", forRemoval = true)
    public DocumentMetadata findOrCreateMetadata(String content, String owner, String originalName, String explicitTopic) {
        // 1. Check for EXACT match first
        Optional<DocumentMetadataMongoEntity> exactMatch = repository.findFirstByContentAndOwner(content, owner);
        if (exactMatch.isPresent()) {
            DocumentMetadataMongoEntity entity = exactMatch.get();
            boolean updated = false;
            // Update originalName if it was missing in the existing record
            if ((entity.getOriginalName() == null || entity.getOriginalName().isBlank()) && originalName != null) {
                entity.setOriginalName(originalName);
                updated = true;
                log.info("Updated missing originalName for existing metadata: {}", originalName);
            }
            if (explicitTopic != null && !explicitTopic.equals(entity.getTopic())) {
                log.info("Updating topic for existing exact match from {} to {}", entity.getTopic(), explicitTopic);
                entity.setTopic(explicitTopic);
                updated = true;
            }
            if (updated) {
                repository.save(entity);
            }
            log.info("Found an exact match");
            return entity.toDomain();
        }

        log.info("[OFFLINE-PIPELINE] Creating text embedding (Vectorization)");
        List<Double> embedding = embeddingPort.embedDocument(content);
        log.info("[OFFLINE-PIPELINE] Embedding created (Dimensions: {})", embedding != null ? embedding.size() : 0);
        String detectedTopic = null;
        List<String> tags = null;

        // 2. Try Vector Search (Similarity)
        try {
            DocumentMetadata similar = searchSimilar(embedding, 1, owner);
            if (similar != null) {
                if (similar.getVectorSearchScore() >= 0.95) {
                    log.info("Reusing exact metadata for content. Topic: {}", similar.getTopic());
                    
                    // Update originalName or topic in DB if missing/different
                    repository.findById(similar.getId()).ifPresent(entity -> {
                        boolean updated = false;
                        if ((entity.getOriginalName() == null || entity.getOriginalName().isBlank()) && originalName != null) {
                            entity.setOriginalName(originalName);
                            updated = true;
                        }
                        if (explicitTopic != null && !explicitTopic.equals(entity.getTopic())) {
                            log.info("Updating topic for similar match from {} to {}", entity.getTopic(), explicitTopic);
                            entity.setTopic(explicitTopic);
                            updated = true;
                        }
                        if (updated) {
                            repository.save(entity);
                        }
                    });
                    
                    if (explicitTopic != null && !explicitTopic.equals(similar.getTopic())) {
                        similar.setTopic(explicitTopic);
                    }
                    
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
            if (explicitTopic != null) {
                detectedTopic = explicitTopic;
                log.info("Using explicit topic: {}", detectedTopic);
            } else {
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
        }

        // 4. Default fallback
        if (detectedTopic == null) {
            detectedTopic = "general:unknown";
            tags = new ArrayList<>();
        }

        List<String> normalizedTags = tagNormalizer.normalizeAll(tags);

        DocumentMetadata domain = DocumentMetadata.builder()
                .content(content)
                .owner(owner)
                .embedding(embedding)
                .topic(detectedTopic)
                .tags(normalizedTags)
                .originalName(originalName)
                .build();

        DocumentMetadataMongoEntity entity = DocumentMetadataMongoEntity.fromDomain(domain);
        DocumentMetadata savedMetadata = repository.save(entity).toDomain();
        
        log.info("[OFFLINE-PIPELINE] Indexing into Lucene & JGraphT...");
        vectorIndexPort.indexDocument(savedMetadata.getId(), embedding, Map.of("title", savedMetadata.getOriginalName()));
        documentGraphPort.addDocumentToGraph(savedMetadata.getId(), embedding, normalizedTags, Set.of());
        
        return savedMetadata;
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
    
    public String deleteByIdAndOwner(String id, String owner) {
    	 return deleteMetadataCommand.execute(id, owner);
    }
}
