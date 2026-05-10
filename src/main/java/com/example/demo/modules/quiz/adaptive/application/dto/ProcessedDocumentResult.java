package com.example.demo.modules.quiz.adaptive.application.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

/**
 * DTO containing the results of processing documents for adaptive quiz generation.
 *
 * <p>This class encapsulates the extracted text, split chunks, sampled content,
 * and metadata identifiers produced during the document processing phase.
 * It serves as the primary data carrier between the document service and
 * the quiz generation logic.
 *
 * @since 1.0
 */
@Getter
@Builder
public class ProcessedDocumentResult {
    /** The combined raw text extracted from all processed documents. */
    private final String aggregatedText;

    /** The complete list of text chunks generated after splitting the documents. */
    private final List<String> allChunks;

    /** A representative subset of chunks selected for AI processing to fit within context limits. */
    private final List<String> sampledChunks;

    /** Unique identifiers for the document metadata records created during processing. */
    private final List<String> individualMetadataIds;

    /** The primary topic identified from the first document, used as a default for the quiz session. */
    private final String firstDetectedTopic;
}
