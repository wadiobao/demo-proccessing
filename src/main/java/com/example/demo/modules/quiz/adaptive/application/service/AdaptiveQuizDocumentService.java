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
 * Service for processing documents, extracting text, and creating metadata.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdaptiveQuizDocumentService {

    private final DocumentProcessingFacade documentProcessingFacade;
    private final DocumentMetadataFacade documentMetadataFacade;
    private final DocumentSplitter documentSplitter;

    /**
     * Processes a list of files and prepares data for quiz generation.
     */
    public ProcessedDocumentResult processDocuments(List<MultipartFile> files, String username, int questionCount) throws Exception {
        log.info("Processing {} documents for user {}", files.size(), username);
        
        List<String> allChunks = new ArrayList<>();
        StringBuilder fullTextBuilder = new StringBuilder();
        List<String> individualMetadataIds = new ArrayList<>();
        String firstDetectedTopic = null;

        for (MultipartFile file : files) {
            String rawText = documentProcessingFacade.processDocument(file).getRawText();
            fullTextBuilder.append(rawText).append("\n\n");
            
            DocumentMetadata meta = documentMetadataFacade.findOrCreateMetadata(rawText, username, file.getOriginalFilename());
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
     * Just extracts text and saves metadata for a list of files.
     */
    public List<String> extractMetadataIds(List<MultipartFile> files, String username) throws Exception {
        log.info("Extracting metadata for {} files for user {}", files.size(), username);
        List<String> individualMetadataIds = new ArrayList<>();
        for (MultipartFile file : files) {
            String rawText = documentProcessingFacade.processDocument(file).getRawText();
            DocumentMetadata meta = documentMetadataFacade.findOrCreateMetadata(rawText, username, file.getOriginalFilename());
            individualMetadataIds.add(meta.getId());
        }
        return individualMetadataIds;
    }

    /**
     * Processes documents from existing metadata IDs (for review flow).
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
