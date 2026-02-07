package com.example.demo.mongo.service.quiz.processor;

import org.springframework.web.multipart.MultipartFile;

/**
 * Strategy interface for processing different document formats.
 */
public interface IDocumentProcessor {

    /**
     * Checks if this processor supports the given file content type.
     *
     * @param contentType MIME type of the file
     * @return true if supported, false otherwise
     */
    boolean supports(String contentType);

    /**
     * Extracts text content from the file.
     *
     * @param file File to process
     * @return Extracted text
     * @throws Exception if processing fails
     */
    String extractText(MultipartFile file) throws Exception;
}
