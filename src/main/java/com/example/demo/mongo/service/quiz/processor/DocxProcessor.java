package com.example.demo.mongo.service.quiz.processor;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Processor for Microsoft Word (DOCX) files.
 * Uses Apache POI for text extraction.
 */
@Component
public class DocxProcessor implements IDocumentProcessor {

    private static final String MIME_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    @Override
    public boolean supports(String contentType) {
        return MIME_TYPE.equals(contentType);
    }

    @Override
    public String extractText(MultipartFile file) throws Exception {
        try (InputStream is = file.getInputStream();
                XWPFDocument doc = new XWPFDocument(is);
                XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {

            return extractor.getText();
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi đọc file DOCX: " + e.getMessage(), e);
        }
    }
}
