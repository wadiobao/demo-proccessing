package com.example.demo.modules.document.processing.infrastructure.adapter.txt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.modules.document.processing.application.port.output.TextExtractorPort;

/**
 * Adapter for extracting text from Plain Text (TXT) files.
 */
@Component
public class TxtAdapter implements TextExtractorPort {

    @Override
    public boolean supports(String extension) {
        return "txt".equalsIgnoreCase(extension);
    }

    @Override
    public String extractText(MultipartFile file) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            throw new IOException("Failed to process TXT file: " + e.getMessage(), e);
        }
    }
}
