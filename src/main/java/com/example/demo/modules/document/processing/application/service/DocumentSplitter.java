package com.example.demo.modules.document.processing.application.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for splitting large documents into manageable knowledge chunks.
 */
@Service
@Slf4j
public class DocumentSplitter {

    private static final int DEFAULT_CHUNK_SIZE = 2500;
    private static final int MIN_CHUNK_SIZE = 500;

    /**
     * Splits text into chunks of roughly equal size with source tagging.
     * 
     * @param text     The raw text to split
     * @param fileName The source file name for tagging
     * @return List of tagged chunks
     */
    public List<String> split(String text, String fileName) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }

        List<String> chunks = new ArrayList<>();
        int length = text.length();

        if (length <= DEFAULT_CHUNK_SIZE) {
            chunks.add(formatChunk(text, fileName));
            return chunks;
        }

        int start = 0;
        while (start < length) {
            int end = Math.min(start + DEFAULT_CHUNK_SIZE, length);
            
            // Try to find a good breaking point (period, newline)
            if (end < length) {
                int lastPeriod = text.lastIndexOf(".", end);
                int lastNewline = text.lastIndexOf("\n", end);
                int breakPoint = Math.max(lastPeriod, lastNewline);
                
                if (breakPoint > start + MIN_CHUNK_SIZE) {
                    end = breakPoint + 1;
                }
            }

            String chunkText = text.substring(start, end).trim();
            if (!chunkText.isBlank()) {
                chunks.add(formatChunk(chunkText, fileName));
            }
            start = end;
        }

        log.debug("Split document '{}' into {} chunks", fileName, chunks.size());
        return chunks;
    }

    private String formatChunk(String text, String fileName) {
        return String.format("[Source: %s]\n%s", fileName, text);
    }
}
