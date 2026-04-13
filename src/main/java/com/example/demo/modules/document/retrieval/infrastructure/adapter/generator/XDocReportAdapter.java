package com.example.demo.modules.document.retrieval.infrastructure.adapter.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Map;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;

import com.example.demo.modules.document.retrieval.application.port.output.DocumentGeneratorPort;

import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import lombok.extern.slf4j.Slf4j;

/**
 * Adapter implementing document generation using XDocReport and Apache POI.
 * 
 * <p>
 * Chuyển đổi và tạo các tệp Word/PDF dựa trên mẫu Freemarker (.docx).
 */
@Component
@Slf4j
public class XDocReportAdapter implements DocumentGeneratorPort {

    @Override
    public boolean supports(String format) {
        return "DOCX".equalsIgnoreCase(format) || "PDF".equalsIgnoreCase(format);
    }

    @Override
    public String generateBase64(String templatePath, Map<String, Object> data, String targetFormat) throws Exception {
        if ("PDF".equalsIgnoreCase(targetFormat)) {
            return exportPdfBase64(templatePath, data);
        } else {
            return generateWordFromTemplateReturnBase64(templatePath, data);
        }
    }

    private String exportPdfBase64(String templatePath, Map<String, Object> data) throws Exception {
        File wordFile = generateWordFromTemplate(templatePath, data);
        File pdfFile = convertDocxToPdf(wordFile);
        try {
            byte[] fileBytes = Files.readAllBytes(pdfFile.toPath());
            return Base64.getEncoder().encodeToString(fileBytes);
        } finally {
            safeDelete(wordFile);
            safeDelete(pdfFile);
        }
    }

    private String generateWordFromTemplateReturnBase64(String templatePath, Map<String, Object> data)
            throws IOException, XDocReportException {
        File wordFile = generateWordFromTemplate(templatePath, data);
        try {
            byte[] fileBytes = Files.readAllBytes(wordFile.toPath());
            return Base64.getEncoder().encodeToString(fileBytes);
        } finally {
            safeDelete(wordFile);
        }
    }

    private File generateWordFromTemplate(String templatePath, Map<String, Object> data)
            throws IOException, XDocReportException {
        InputStream templateFile = getClass().getResourceAsStream(templatePath);
        if (templateFile == null)
            throw new FileNotFoundException("Template not found: " + templatePath);

        IXDocReport report = XDocReportRegistry.getRegistry().loadReport(templateFile, TemplateEngineKind.Freemarker);

        IContext context = report.createContext();
        data.forEach(context::put);
        File wordFile = File.createTempFile("xdoc", ".docx");
        try (OutputStream out = new FileOutputStream(wordFile)) {
            report.process(context, out);
        }

        return wordFile;
    }

    private File convertDocxToPdf(File wordFile) throws Exception {
        File pdfFile = File.createTempFile("xdoc_", ".pdf");
        try (InputStream docxInputStream = new FileInputStream(wordFile);
                OutputStream pdfOutputStream = new FileOutputStream(pdfFile)) {

            XWPFDocument document = new XWPFDocument(docxInputStream);
            PdfOptions options = PdfOptions.create();
            PdfConverter.getInstance().convert(document, pdfOutputStream, options);
        }
        return pdfFile;
    }

    private void safeDelete(File file) {
        if (file != null && file.exists()) {
            if (!file.delete()) {
                log.warn("Failed to delete temporary file: {}", file.getAbsolutePath());
            }
        }
    }
}
