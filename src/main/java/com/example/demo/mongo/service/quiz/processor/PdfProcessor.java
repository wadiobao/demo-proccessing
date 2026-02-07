package com.example.demo.mongo.service.quiz.processor;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.constants.Constants;
import com.example.demo.mongo.service.quiz.PDFProcessingService;

import lombok.RequiredArgsConstructor;

/**
 * Processor for PDF files.
 * Supports both standard text PDFs and scanned images (via Refactored
 * PDFProcessingService).
 */
@Component
@RequiredArgsConstructor
public class PdfProcessor implements IDocumentProcessor {

    private final PDFProcessingService pdfProcessingService;

    @Override
    public boolean supports(String contentType) {
        return Constants.FileTypes.PDF.equals(contentType);
    }

    @Override
    public String extractText(MultipartFile file) throws Exception {
        String pdfType = pdfProcessingService.checkPDF(file);

        if ("BASE".equals(pdfType)) {
            // Standard PDF
            try (PDDocument document = PDDocument.load(file.getInputStream())) {
                PDFTextStripper stripper = new PDFTextStripper();
                return stripper.getText(document);
            }
        } else {
            // Scanned PDF (OCR)
            return pdfProcessingService.renderPdfToPngToString(file);
        }
    }
}
