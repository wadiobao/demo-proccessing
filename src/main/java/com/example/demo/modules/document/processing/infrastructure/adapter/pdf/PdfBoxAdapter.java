package com.example.demo.modules.document.processing.infrastructure.adapter.pdf;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.modules.document.processing.application.port.output.TextExtractorPort;
import com.example.demo.modules.document.processing.infrastructure.util.ImageProcessor;
import com.example.demo.constants.Constants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

/**
 * Adapter for extracting text from PDF documents using PDFBox and Tesseract OCR.
 * 
 * <p>
 * Implement chiến lược trích xuất kép: văn bản thô cho PDF chuẩn 
 * và OCR cho PDF dạng scan sau khi đã xử lý hình ảnh.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PdfBoxAdapter implements TextExtractorPort {

    private final ImageProcessor imageProcessor;

    @Override
    public boolean supports(String extension) {
        return "pdf".equalsIgnoreCase(extension);
    }

    @Override
    public String extractText(MultipartFile file) throws IOException {
        String pdfType = checkPdfType(file);

        if ("BASE".equals(pdfType)) {
            return extractRawText(file);
        } else {
            return performOcr(file);
        }
    }

    private String checkPdfType(MultipartFile file) {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            return (text == null || text.trim().isEmpty()) ? "SCAN" : "BASE";
        } catch (IOException e) {
            log.error("Error detecting PDF type: {}", e.getMessage());
            return "UNKNOWN";
        }
    }

    private String extractRawText(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String performOcr(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            StringBuilder pdfStringBuilder = new StringBuilder();

            int pageCount = document.getNumberOfPages();
            ExecutorService executor = Executors.newFixedThreadPool(Math.min(pageCount, 4));
            List<Future<String>> results = new ArrayList<>();

            for (int i = 0; i < pageCount; i++) {
                final int index = i;
                BufferedImage rawImg = pdfRenderer.renderImageWithDPI(index, 300);

                // Image preprocessing for better OCR results
                BufferedImage processedImg = imageProcessor.toGrayscale(rawImg);
                processedImg = imageProcessor.medianFilter(processedImg);
                processedImg = imageProcessor.binaryThreshold(processedImg);

                final BufferedImage finalImg = processedImg;

                results.add(executor.submit(() -> {
                    try {
                        Tesseract tesseract = new Tesseract();
                        tesseract.setDatapath(Constants.FilePaths.TESSDATA_PATH);
                        tesseract.setLanguage(Constants.Languages.VIETNAMESE);
                        return String.format(Constants.Messages.PAGE_FORMAT, index + 1, tesseract.doOCR(finalImg));
                    } catch (TesseractException e) {
                        log.error("OCR Error on page {}: {}", index + 1, e.getMessage());
                        return "Error OCR Page " + (index + 1);
                    }
                }));
                rawImg.flush();
            }

            for (Future<String> result : results) {
                try {
                    pdfStringBuilder.append(result.get());
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Thread execution error: {}", e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
            executor.shutdown();
            return pdfStringBuilder.toString();
        }
    }
}
