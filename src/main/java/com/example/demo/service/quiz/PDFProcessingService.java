package com.example.demo.service.quiz;

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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.constants.Constants;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

@Service
public class PDFProcessingService {
    
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
            final BufferedImage img = pdfRenderer.renderImageWithDPI(index, 100);

            results.add(executor.submit(() -> {
                Tesseract t = new Tesseract();
                t.setDatapath(Constants.FilePaths.TESSDATA_PATH);
                t.setLanguage(Constants.Languages.VIETNAMESE);
                return String.format(Constants.Messages.PAGE_FORMAT, index + 1, t.doOCR(img));
            }));
            img.flush();
        }

        for (Future<String> result : results) {
            pdfString += result.get();
        }
        executor.shutdown();
        document.close();
        return pdfString;
    }
} 