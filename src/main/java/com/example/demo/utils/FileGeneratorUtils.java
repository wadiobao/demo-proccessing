package com.example.demo.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;

/**
 * Document generation engine for quiz exports and media processing.
 * 
 * <p>
 * Tích hợp các thư viện XDocReport và POI để tạo file Word từ template
 * và chuyển đổi sang định dạng PDF, hỗ trợ xuất dữ liệu Base64 cho Frontend.
 *
 * @since 1.0
 */
@Component
@Slf4j
public class FileGeneratorUtils {

    @Autowired
    CloudinaryUtils cloudinaryUtils;

    /**
     * Generates a PDF document from a template and returns it as a Base64 string.
     * 
     * @param templatePath path to the XDoc template / đường dẫn tới mẫu văn bản
     * @param data         map of variables to inject / bản đồ dữ liệu cần chèn vào
     *                     mẫu
     * @return Base64 encoded PDF string / chuỗi PDF định dạng Base64
     * @throws Exception if generation or conversion fails / lỗi khi tạo hoặc chuyển
     *                   đổi file
     */
    public String exportPdfBase64(String templatePath, Map<String, Object> data) throws Exception {
        File wordFile = generateWordFromTemplate(templatePath, data);
        File pdfFile = convertDocxToPdf(wordFile);
        try {
            byte[] fileBytes = Files.readAllBytes(pdfFile.toPath());
            return Base64.getEncoder().encodeToString(fileBytes);
        } finally {
            // enforce local cleanup to prevent storage accumulation on the server
            // / cưỡng chế xóa file tạm để tránh tích tụ dữ liệu trên server
            safeDelete(wordFile);
            safeDelete(pdfFile);
        }
    }

    /**
     * Injects data into a Word template and returns the result as a Base64 string.
     * 
     * @param templatePath path to the XDoc template / đường dẫn mẫu văn bản
     * @param data         variables for interpolation / dữ liệu cần chèn
     * @return Base64 encoded Docx string / chuỗi Docx định dạng Base64
     * @throws IOException         for file access issues / lỗi truy cập file
     * @throws XDocReportException for template processing errors / lỗi xử lý mẫu
     */
    public String generateWordFromTemplateReturnBase64(String templatePath, Map<String, Object> data)
            throws IOException, XDocReportException {
        InputStream templateFile = getClass().getResourceAsStream(templatePath);
        if (templateFile == null)
            throw new FileNotFoundException("Template not found" + templatePath);

        IXDocReport report = XDocReportRegistry.getRegistry().loadReport(templateFile, TemplateEngineKind.Freemarker);

        IContext context = report.createContext();
        data.forEach((t, u) -> context.put(t, u));
        File wordFile = File.createTempFile("xdoc", ".docx");
        try (OutputStream out = new FileOutputStream(wordFile)) {
            report.process(context, out);
        }

        byte[] fileBytes = Files.readAllBytes(wordFile.toPath());
        safeDelete(wordFile);

        return Base64.getEncoder().encodeToString(fileBytes);
    }

    /**
     * Processes an XDoc template and creates a temporary Word file.
     * 
     * @param templatePath path to resource / đường dẫn tài nguyên mẫu
     * @param data         variables for interpolation / dữ liệu cần chèn
     * @return temporary File object / đối tượng file tạm thời
     * @throws IOException         for file system errors / lỗi hệ thống tập tin
     * @throws XDocReportException for processing failures / lỗi xử lý dữ liệu
     */
    public File generateWordFromTemplate(String templatePath, Map<String, Object> data)
            throws IOException, XDocReportException {
        InputStream templateFile = getClass().getResourceAsStream(templatePath);
        if (templateFile == null)
            throw new FileNotFoundException("Template not found" + templatePath);

        IXDocReport report = XDocReportRegistry.getRegistry().loadReport(templateFile, TemplateEngineKind.Freemarker);

        IContext context = report.createContext();
        data.forEach((t, u) -> context.put(t, u));
        File wordFile = File.createTempFile("xdoc", ".docx");
        try (OutputStream out = new FileOutputStream(wordFile)) {
            report.process(context, out);
        }

        return wordFile;
    }

    /**
     * Converts an existing Word document to PDF format.
     * 
     * @param wordFile source Docx file / file Word nguồn
     * @return newly created PDF file object / đối tượng file PDF mới tạo
     * @throws Exception for conversion library errors / lỗi thư viện chuyển đổi
     */
    public File convertDocxToPdf(File wordFile) throws Exception {
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
            file.delete();
        }
    }

    /**
     * Decodes a Base64 image, saves it locally, and uploads to Cloudinary.
     * 
     * @param base64String raw image data / dữ liệu ảnh Base64
     * @param fileName     target local filename / tên file tạm cục bộ
     * @return Cloudinary metadata [public_id, secure_url] / thông tin định danh và
     *         URL từ Cloudinary
     */
    public String[] saveImageFromBase64(String base64String, String fileName) {
        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64String);
            Files.write(Paths.get(fileName), imageBytes);
            String imgAtt[] = cloudinaryUtils.upload(fileName);
            if (Files.deleteIfExists(Paths.get(fileName)))
                log.info("Temporary image file deleted from local storage.");

            return imgAtt;
        } catch (IOException e) {
            log.error("Error saving image file: {}", e.getMessage());
        }
        return null;
    }

}
