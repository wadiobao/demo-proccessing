package com.example.demo.modules.quiz.adaptive.application.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

/**
 * Result object for document processing in adaptive quiz generation.
 */
@Getter
@Builder
public class ProcessedDocumentResult {
    private final String aggregatedText;
    private final List<String> allChunks;
    private final List<String> sampledChunks;
    private final List<String> individualMetadataIds;
    private final String firstDetectedTopic;
}
