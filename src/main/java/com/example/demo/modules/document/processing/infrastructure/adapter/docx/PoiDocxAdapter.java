package com.example.demo.modules.document.processing.infrastructure.adapter.docx;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.modules.document.processing.application.port.output.TextExtractorPort;

import lombok.extern.slf4j.Slf4j;

/**
 * Adapter for extracting text from Microsoft Word (DOCX) files using Apache POI.
 */
@Component
@Slf4j
public class PoiDocxAdapter implements TextExtractorPort {

    @Override
    public boolean supports(String extension) {
        return "docx".equalsIgnoreCase(extension);
    }

    @Override
    public String extractText(MultipartFile file) throws IOException {
        log.info("Extracting text from DOCX: {}", file.getOriginalFilename());
        try (InputStream is = file.getInputStream();
                XWPFDocument doc = new XWPFDocument(is);
                XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {

            return extractor.getText();
        } catch (Exception e) {
            log.error("Error processing DOCX file: {}", e.getMessage());
            throw new IOException("Failed to process DOCX file: " + e.getMessage(), e);
        }
    }
}
