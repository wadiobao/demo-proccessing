package com.example.demo.mongo.service.quiz.processor;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

/**
 * Factory for selecting the appropriate document processor based on file type.
 */
@Component
@RequiredArgsConstructor
public class DocumentProcessorContext {

    private final List<IDocumentProcessor> processors;

    /**
     * Gets the appropriate processor for the given file.
     *
     * @param file File to process
     * @return Supporting IDocumentProcessor
     * @throws IllegalArgumentException if no processor supports the file type
     */
    public IDocumentProcessor getProcessor(MultipartFile file) {
        String contentType = file.getContentType();

        // Handle edge case where content type might be null or vague
        if (contentType == null) {
            String filename = file.getOriginalFilename();
            if (filename != null && filename.toLowerCase().endsWith(".txt")) {
                contentType = "text/plain";
            }
        }

        for (IDocumentProcessor processor : processors) {
            if (processor.supports(contentType)) {
                return processor;
            }
        }

        throw new IllegalArgumentException("Định dạng file không được hỗ trợ: " + contentType);
    }
}
