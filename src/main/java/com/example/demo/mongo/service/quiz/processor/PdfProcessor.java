package com.example.demo.mongo.service.quiz.processor;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.constants.Constants;
import com.example.demo.mongo.service.quiz.PDFProcessingService;

import lombok.RequiredArgsConstructor;

/**
 * Implementation of document processor for PDF documents.
 * 
 * <p>
 * Tích hợp hai chiến lược: Trích xuất trực tiếp cho PDF văn bản và
 * kích hoạt luồng OCR cho PDF dạng ảnh chụp/scan.
 *
 * @since 1.0
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
