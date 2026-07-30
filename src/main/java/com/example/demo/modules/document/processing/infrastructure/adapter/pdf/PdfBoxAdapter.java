package com.example.demo.modules.document.processing.infrastructure.adapter.pdf;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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

        // Tesseract khởi tạo 1 lần/thread, tái sử dụng cho mọi trang thread đó xử lý
        // (tránh load lại tessdata tiếng Việt mỗi trang — chi phí I/O + memory không
        // nhỏ)
        ThreadLocal<Tesseract> tesseractThreadLocal = ThreadLocal.withInitial(() -> {
            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath(Constants.FilePaths.TESSDATA_PATH);
            tesseract.setLanguage(Constants.Languages.VIETNAMESE);
            tesseract.setVariable("user_defined_dpi", "300");
            return tesseract;
        });

        try (PDDocument document = PDDocument.load(file.getInputStream())) { // PDFBox 2.x
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            int pageCount = document.getNumberOfPages();

            List<CompletableFuture<PageOcrResult>> futures = new ArrayList<>();

            // Đưa TOÀN BỘ pipeline (render + tiền xử lý + OCR) vào async task
            // -> tận dụng đa luồng cho cả bước render, không chỉ riêng OCR
            for (int i = 0; i < pageCount; i++) {
                final int pageIndex = i;

                CompletableFuture<PageOcrResult> futureTask = CompletableFuture.supplyAsync(() -> {
                    BufferedImage rawImg = null;
                    BufferedImage processedImg = null;
                    try {
                        // a. Tính DPI an toàn theo kích thước trang, chống OOM
                        PDPage page = document.getPage(pageIndex);
                        PDRectangle box = page.getMediaBox();
                        float widthInInches = box.getWidth() / 72f;
                        float heightInInches = box.getHeight() / 72f;

                        int dpi = 300;
                        float maxExpectedPixels = Math.max(widthInInches * dpi, heightInInches * dpi);
                        if (maxExpectedPixels > 4000) {
                            dpi = Math.max(72, (int) (300 * (4000 / maxExpectedPixels)));
                            log.warn("Trang {} quá lớn ({}x{} inches). Giảm DPI xuống {} để chống sập RAM.",
                                    pageIndex + 1, widthInInches, heightInInches, dpi);
                        }

                        // b. Render ảnh trong thread con.
                        // PDFRenderer của PDFBox 2.x KHÔNG đảm bảo thread-safe khi gọi
                        // đồng thời trên cùng 1 PDDocument -> bắt buộc synchronized ở đây.
                        synchronized (document) {
                            rawImg = pdfRenderer.renderImageWithDPI(pageIndex, dpi);
                        }

                        // c. Tiền xử lý ảnh trong thread con
                        processedImg = imageProcessor.toGrayscale(rawImg);
                        processedImg = imageProcessor.medianFilter(processedImg);
                        processedImg = imageProcessor.binaryThreshold(processedImg);

                        // d. OCR dùng Tesseract tái sử dụng từ ThreadLocal
                        Tesseract tesseract = tesseractThreadLocal.get();
                        String text = tesseract.doOCR(processedImg);

                        String formatted = String.format(Constants.Messages.PAGE_FORMAT, pageIndex + 1, text);
                        return new PageOcrResult(pageIndex, formatted);

                    } catch (Exception e) {
                        // Cô lập lỗi theo từng trang — 1 trang hỏng không làm mất
                        // kết quả các trang khác đã xử lý xong
                        log.error("OCR Error on page {}: {}", pageIndex + 1, e.getMessage(), e);
                        return new PageOcrResult(pageIndex, "Error OCR Page " + (pageIndex + 1) + "\n");
                    } finally {
                        // Giải phóng RAM ngay sau khi xong trang đó — cả 2 ảnh, không chỉ rawImg
                        if (rawImg != null)
                            rawImg.flush();
                        if (processedImg != null)
                            processedImg.flush();
                    }
                }, threadPoolTaskExecutor);

                futures.add(futureTask);
            }

            // Chờ tất cả các trang hoàn thành
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // Sắp xếp lại tường minh theo pageIndex — an toàn tuyệt đối bất kể
            // thread nào hoàn thành trước, tránh lệch thứ tự trang
            List<PageOcrResult> results = futures.stream()
                    .map(CompletableFuture::join)
                    .sorted(Comparator.comparingInt(PageOcrResult::pageIndex))
                    .toList();

            StringBuilder pdfStringBuilder = new StringBuilder();
            for (PageOcrResult result : results) {
                pdfStringBuilder.append(result.text());
            }

            return pdfStringBuilder.toString();

        } finally {
            tesseractThreadLocal.remove();
        }
    }

    private record PageOcrResult(int pageIndex, String text) {
    }
}
