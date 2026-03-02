package com.example.demo.mongo.service.quiz;

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

import com.example.demo.constants.Constants;
import com.example.demo.utils.ImageProcessingUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

/**
 * Service for extracting text from PDF documents using multiple strategies.
 * 
 * <p>
 * Hỗ trợ trích xuất văn bản trực tiếp từ PDF dạng text và sử dụng
 * OCR (Tesseract) cho các tài liệu dạng scan đã qua xử lý hình ảnh.
 *
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PDFProcessingService {

    ImageProcessingUtils imageProcessingUtils;

    /**
     * Extracts raw text from a standard (non-scanned) PDF file.
     * 
     * @param pdfFile uploaded document / file tải lên
     * @return plain text content / nội dung văn bản thô
     * @throws IOException for reading errors / lỗi đọc file
     */
    public String extractTextFromPdf(MultipartFile pdfFile) throws IOException {
        try (PDDocument document = PDDocument.load(pdfFile.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    public String checkPDF(MultipartFile file) {
        try {
            PDDocument document = PDDocument.load(file.getInputStream());
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            document.close();

            return text.trim().isEmpty() ? "SCAN" : "BASE";
        } catch (IOException e) {
            return "Lỗi khi đọc file PDF: " + e.getMessage();
        }
    }

    /**
     * Performs Optical Character Recognition (OCR) on a scanned PDF.
     * 
     * <p>
     * Chuyển đổi các trang PDF thành hình ảnh độ phân giải cao (300 DPI),
     * áp dụng các bộ lọc xử lý ảnh và trích xuất chữ bằng Tesseract.
     *
     * @param pdfFile scanned document / tài liệu dạng scan
     * @return OCR-extracted text / văn bản trích xuất từ OCR
     * @throws Exception for multi-threading or OCR failures / lỗi đa luồng hoặc lỗi
     *                   nhận diện
     */
    public String renderPdfToPngToString(MultipartFile pdfFile)
            throws IOException, TesseractException, InterruptedException, ExecutionException {
        PDDocument document = PDDocument.load(pdfFile.getInputStream());
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        String pdfString = "";

        int pageCount = document.getNumberOfPages();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        List<Future<String>> results = new ArrayList<>();

        for (int i = 0; i < pageCount; i++) {
            final int index = i;
            BufferedImage rawImg = pdfRenderer.renderImageWithDPI(index, 300);

            // Pre-processing pipeline
            BufferedImage processedImg = imageProcessingUtils.toGrayscale(rawImg);
            processedImg = imageProcessingUtils.medianFilter(processedImg);
            processedImg = imageProcessingUtils.binaryThreshold(processedImg);

            final BufferedImage finalImg = processedImg;

            results.add(executor.submit(() -> {
                try {
                    Tesseract t = new Tesseract();
                    t.setDatapath(Constants.FilePaths.TESSDATA_PATH);
                    t.setLanguage(Constants.Languages.VIETNAMESE);
                    return String.format(Constants.Messages.PAGE_FORMAT, index + 1, t.doOCR(finalImg));
                } catch (TesseractException e) {
                    return "Error OCR Page " + (index + 1) + ": " + e.getMessage();
                }
            }));
            rawImg.flush();
        }

        for (Future<String> result : results) {
            pdfString += result.get();
        }
        executor.shutdown();
        document.close();
        return pdfString;
    }
}
