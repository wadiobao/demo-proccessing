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
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.constants.Constants;
import com.example.demo.modules.document.processing.application.port.output.TextExtractorPort;
import com.example.demo.modules.document.processing.infrastructure.util.ImageProcessor;
import com.itextpdf.text.pdf.PdfPage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

/**
 * Adapter for extracting text from PDF documents using PDFBox and Tesseract
 * OCR.
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
    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;

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
            List<Future<String>> results = new ArrayList<>();

            for (int i = 0; i < pageCount; i++) {
                final int index = i;

                PDPage page = document.getPage(index);
                PDRectangle box = page.getMediaBox();

                // 1 point = 1/72 inch
                float widthInInches = box.getWidth() / 72f;
                float heightInInches = box.getHeight() / 72f;

                int dpi = 300;
                float maxExpectedPixels = Math.max(widthInInches * dpi, heightInInches * dpi);

                // Giới hạn kích thước ảnh tối đa 4000 pixels để tránh sập RAM (OOM DoS)
                if (maxExpectedPixels > 4000) {
                    dpi = (int) (300 * (4000 / maxExpectedPixels));
                    if (dpi < 72)
                        dpi = 72; // Cố định DPI tối thiểu để vẫn đọc được text
                    log.warn("Trang {} quá lớn ({}x{} inches). Giảm DPI xuống {} để chống sập RAM.",
                            index + 1, widthInInches, heightInInches, dpi);
                }

                BufferedImage rawImg = pdfRenderer.renderImageWithDPI(index, dpi);

                // Image preprocessing for better OCR results
                BufferedImage processedImg = imageProcessor.toGrayscale(rawImg);
                processedImg = imageProcessor.medianFilter(processedImg);
                processedImg = imageProcessor.binaryThreshold(processedImg);

                final BufferedImage finalImg = processedImg;

                results.add(threadPoolTaskExecutor.submit(() -> {
                    try {
                        Tesseract tesseract = new Tesseract();
                        tesseract.setDatapath(Constants.FilePaths.TESSDATA_PATH);
                        tesseract.setLanguage(Constants.Languages.VIETNAMESE);
                        tesseract.setVariable("user_defined_dpi", "300");
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
                    log.error("Lỗi khi chờ Thread OCR: {}", e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
            return pdfStringBuilder.toString();
        }
    }
}
