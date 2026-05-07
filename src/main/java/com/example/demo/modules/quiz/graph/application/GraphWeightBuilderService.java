package com.example.demo.modules.quiz.graph.application;

import java.util.List;
import java.util.Optional;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.demo.modules.quiz.graph.infrastructure.persistence.entity.Tag;
import com.example.demo.modules.quiz.graph.infrastructure.persistence.entity.TagRelation;
import com.example.demo.modules.quiz.graph.infrastructure.persistence.repository.TagRelationRepository;
import com.example.demo.modules.quiz.graph.infrastructure.persistence.repository.TagRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for building and updating the Co-occurrence Tag Graph.
 * 
 * <p>Processes combinations of tags to update network weights asynchronously.
 * 
 * @since 1.2
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GraphWeightBuilderService {

    private final TagRepository tagRepository;
    private final TagRelationRepository tagRelationRepository;

    /**
     * Rebuilds or increments edges natively by extracting pairs of tags
     * from a documented tag list.
     * 
     * @param tags extracted tags from a document
     */
    @Async
    public void buildGraphFromDocumentTags(List<String> tags) {
        if (tags == null || tags.size() < 2) {
            return;
        }
        
        log.info("Starting Async Graph Edge weight increment for {} tags", tags.size());

        // Normalize text
        List<String> normalizedTags = tags.stream()
                .map(String::toLowerCase)
                .map(String::trim)
                .distinct()
                .toList();

        // 1. Ensure Tag nodes exist in DB
        for (String tagName : normalizedTags) {
            Tag tag = tagRepository.findByName(tagName).orElse(null);
            if (tag == null) {
                tag = Tag.builder().name(tagName).usageCount(1L).build();
                tagRepository.save(tag);
            } else {
                tag.setUsageCount(tag.getUsageCount() + 1);
                tagRepository.save(tag);
            }
        }

        // 2. Map combinatorics (Pairs)
        for (int i = 0; i < normalizedTags.size(); i++) {
            for (int j = i + 1; j < normalizedTags.size(); j++) {
                String tag1 = normalizedTags.get(i);
                String tag2 = normalizedTags.get(j);

                // Alphabetical sorting to prevent A-B vs B-A duplication
                String nodeA = tag1.compareTo(tag2) <= 0 ? tag1 : tag2;
                String nodeB = tag1.compareTo(tag2) > 0 ? tag1 : tag2;

                upsertEdge(nodeA, nodeB);
            }
        }
        
        log.info("Finished Async Graph Edge builder.");
    }

    private void upsertEdge(String nodeA, String nodeB) {
        Optional<TagRelation> optionalEdge = tagRelationRepository.findByTagsPair(nodeA, nodeB);

        if (optionalEdge.isPresent()) {
            TagRelation edge = optionalEdge.get();
            edge.setWeightScore(edge.getWeightScore() + 1);
            tagRelationRepository.save(edge);
        } else {
            TagRelation newEdge = TagRelation.builder()
                .tag1Name(nodeA)
                .tag2Name(nodeB)
                .weightScore(1L)
                .build();
            tagRelationRepository.save(newEdge);
        }
    }
}
