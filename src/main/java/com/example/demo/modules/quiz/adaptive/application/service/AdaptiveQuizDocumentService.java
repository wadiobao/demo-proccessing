package com.example.demo.modules.quiz.adaptive.application.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.modules.document.metadata.api.DocumentMetadataFacade;
import com.example.demo.modules.document.metadata.domain.model.DocumentMetadata;
import com.example.demo.modules.document.processing.api.DocumentProcessingFacade;
import com.example.demo.modules.document.processing.application.service.DocumentSplitter;
import com.example.demo.modules.quiz.adaptive.application.dto.ProcessedDocumentResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Manages document processing and metadata extraction for adaptive quiz generation.
 *
 * <p>This service coordinates between document processing, metadata management,
 * and document splitting to prepare content for AI-driven quiz generation.
 * It handles both new file uploads and existing document metadata.
 *
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdaptiveQuizDocumentService {

    private final DocumentProcessingFacade documentProcessingFacade;
    private final DocumentMetadataFacade documentMetadataFacade;
    private final DocumentSplitter documentSplitter;

    /**
     * Processes uploaded files to extract text, create metadata, and sample content chunks.
     *
     * @param files the list of multipart files to be processed
     * @param username the identifier of the user who owns the documents
     * @param questionCount the number of questions intended for generation, used to determine sampling size
     * @return a {@link ProcessedDocumentResult} containing aggregated text, chunks, and metadata identifiers
     * @throws Exception if document processing or metadata creation fails
     */
    public ProcessedDocumentResult processDocuments(List<MultipartFile> files, String username, int questionCount) throws Exception {
        return processDocuments(files, username, questionCount, null);
    }

    public ProcessedDocumentResult processDocuments(List<MultipartFile> files, String username, int questionCount, String topic) throws Exception {
        log.info("Processing {} documents for user {} (Topic: {})", files.size(), username, topic);
        
        List<String> allChunks = new ArrayList<>();
        StringBuilder fullTextBuilder = new StringBuilder();
        List<String> individualMetadataIds = new ArrayList<>();
        String firstDetectedTopic = null;

        for (MultipartFile file : files) {
            String rawText = documentProcessingFacade.processDocument(file).getRawText();
            fullTextBuilder.append(rawText).append("\n\n");
            
            DocumentMetadata meta = documentMetadataFacade.findOrCreateMetadata(rawText, username, file.getOriginalFilename(), topic);
            individualMetadataIds.add(meta.getId());
            if (firstDetectedTopic == null) {
                firstDetectedTopic = meta.getTopic();
            }

            List<String> fileChunks = documentSplitter.split(rawText, file.getOriginalFilename());
            allChunks.addAll(fileChunks);
        }

        String aggregatedText = fullTextBuilder.toString();
        int dynamicMaxChunks = Math.min(60, Math.max(15, (int) (questionCount * 1.5)));
        List<String> sampledChunks = performUniformSampling(allChunks, dynamicMaxChunks);

        return ProcessedDocumentResult.builder()
                .aggregatedText(aggregatedText)
                .allChunks(allChunks)
                .sampledChunks(sampledChunks)
                .individualMetadataIds(individualMetadataIds)
                .firstDetectedTopic(firstDetectedTopic)
                .build();
    }

    /**
     * Extracts text and generates metadata identifiers for a set of uploaded files.
     *
     * @param files the list of multipart files to analyze
     * @param username the identifier of the user who owns the documents
     * @return a list of unique metadata identifiers for the processed files
     * @throws Exception if file processing or metadata storage fails
     */
    public List<String> extractMetadataIds(List<MultipartFile> files, String username) throws Exception {
        return extractMetadataIds(files, username, null);
    }

    public List<String> extractMetadataIds(List<MultipartFile> files, String username, String topic) throws Exception {
        log.info("Extracting metadata for {} files for user {} (Topic: {})", files.size(), username, topic);
        List<String> individualMetadataIds = new ArrayList<>();
        for (MultipartFile file : files) {
            String rawText = documentProcessingFacade.processDocument(file).getRawText();
            DocumentMetadata meta = documentMetadataFacade.findOrCreateMetadata(rawText, username, file.getOriginalFilename(), topic);
            individualMetadataIds.add(meta.getId());
        }
        return individualMetadataIds;
    }

    /**
     * Reconstructs processing data from existing metadata identifiers.
     *
     * <p>This is typically used in review or re-generation flows where the
     * documents have already been processed and stored.
     *
     * @param metadataIds list of existing document metadata identifiers
     * @param questionCount the number of questions intended for generation
     * @return a {@link ProcessedDocumentResult} containing the reconstructed document data
     */
    public ProcessedDocumentResult processFromMetadataIds(List<String> metadataIds, int questionCount) {
        log.info("Processing {} existing metadata documents", metadataIds.size());
        
        List<String> allChunks = new ArrayList<>();
        StringBuilder fullTextBuilder = new StringBuilder();
        String firstDetectedTopic = null;

        List<DocumentMetadata> metadatas = documentMetadataFacade.findByIds(metadataIds);
        for (DocumentMetadata meta : metadatas) {
            String content = meta.getContent();
            fullTextBuilder.append(content).append("\n\n");
            
            if (firstDetectedTopic == null) {
                firstDetectedTopic = meta.getTopic();
            }

            // For existing metadata, we rely on the content being already split/stored or we split it again
            // Here we split it again to get the chunks for the AI
            List<String> fileChunks = documentSplitter.split(content, meta.getOriginalName());
            allChunks.addAll(fileChunks);
        }

        String aggregatedText = fullTextBuilder.toString();
        int dynamicMaxChunks = Math.min(60, Math.max(15, (int) (questionCount * 1.5)));
        List<String> sampledChunks = performUniformSampling(allChunks, dynamicMaxChunks);

        return ProcessedDocumentResult.builder()
                .aggregatedText(aggregatedText)
                .allChunks(allChunks)
                .sampledChunks(sampledChunks)
                .individualMetadataIds(metadataIds)
                .firstDetectedTopic(firstDetectedTopic)
                .build();
    }
    /**
     * Selects a representative subset of text chunks using uniform sampling.
     *
     * @param allChunks the complete list of text chunks
     * @param maxChunks the maximum number of chunks to include in the sample
     * @return a list of sampled text chunks
     */
    private List<String> performUniformSampling(List<String> allChunks, int maxChunks) {
        if (allChunks.size() <= maxChunks) {
            return allChunks;
        }
        List<String> sampled = new ArrayList<>();
        double step = (double) (allChunks.size() - 1) / (maxChunks - 1);
        for (int i = 0; i < maxChunks; i++) {
            int index = (int) Math.round(i * step);
            sampled.add(allChunks.get(index));
        }
        return sampled;
    }
}
