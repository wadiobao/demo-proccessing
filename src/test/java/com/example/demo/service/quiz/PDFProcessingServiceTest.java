package com.example.demo.service.quiz;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

@ExtendWith(MockitoExtension.class)
class PDFProcessingServiceTest {

    @InjectMocks
    private PDFProcessingService pdfProcessingService;

    private MockMultipartFile mockPdfFile;

    @BeforeEach
    void setUp() {
        mockPdfFile = new MockMultipartFile("file", "test.pdf", "application/pdf", "pdf content".getBytes());
    }

    @Test
    void extractTextFromPdf_ShouldReturnExtractedText() throws IOException {
        try (MockedStatic<PDDocument> mockedStatic = mockStatic(PDDocument.class)) {
            PDDocument mockDocument = mock(PDDocument.class);
            PDFTextStripper mockStripper = mock(PDFTextStripper.class);

            mockedStatic.when(() -> PDDocument.load(any(InputStream.class))).thenReturn(mockDocument);
            when(mockStripper.getText(mockDocument)).thenReturn("Extracted text");

            PDFTextStripper originalStripper = new PDFTextStripper(); // Create a real instance
            // Use ReflectionTestUtils to set the stripper field if it was injected
            // For now, we'll assume it's created internally or passed as a dependency

            String result = pdfProcessingService.extractTextFromPdf(mockPdfFile);

            assertEquals("Extracted text", result);
            verify(mockDocument, times(1)).close();
        }
    }

    @Test
    void checkPDF_EmptyText_ShouldReturnSCAN() throws IOException {
        try (MockedStatic<PDDocument> mockedStatic = mockStatic(PDDocument.class)) {
            PDDocument mockDocument = mock(PDDocument.class);
            PDFTextStripper mockStripper = mock(PDFTextStripper.class);

            mockedStatic.when(() -> PDDocument.load(any(InputStream.class))).thenReturn(mockDocument);
            when(mockStripper.getText(mockDocument)).thenReturn("   "); // Empty text with spaces

            String result = pdfProcessingService.checkPDF(mockPdfFile);

            assertEquals("SCAN", result);
            verify(mockDocument, times(1)).close();
        }
    }

    @Test
    void checkPDF_NonEmptyText_ShouldReturnBASE() throws IOException {
        try (MockedStatic<PDDocument> mockedStatic = mockStatic(PDDocument.class)) {
            PDDocument mockDocument = mock(PDDocument.class);
            PDFTextStripper mockStripper = mock(PDFTextStripper.class);

            mockedStatic.when(() -> PDDocument.load(any(InputStream.class))).thenReturn(mockDocument);
            when(mockStripper.getText(mockDocument)).thenReturn("Some text");

            String result = pdfProcessingService.checkPDF(mockPdfFile);

            assertEquals("BASE", result);
            verify(mockDocument, times(1)).close();
        }
    }

    @Test
    void checkPDF_IOException_ShouldReturnErrorMessage() throws IOException {
        try (MockedStatic<PDDocument> mockedStatic = mockStatic(PDDocument.class)) {
            mockedStatic.when(() -> PDDocument.load(any(InputStream.class))).thenThrow(new IOException("Test IO Exception"));

            String result = pdfProcessingService.checkPDF(mockPdfFile);

            assertTrue(result.startsWith("Lỗi khi đọc file PDF:"));
        }
    }

    }
// Note: The renderPdfToPngToString method is difficult to unit test effectively with standard Mockito
// due to its internal creation of PDFRenderer and Tesseract instances.
// For comprehensive testing, consider refactoring the PDFProcessingService to allow these dependencies to be injected.
