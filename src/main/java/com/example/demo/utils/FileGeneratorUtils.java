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

import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;


@Component
public class FileGeneratorUtils {
	
	@Autowired
	CloudinaryUtils cloudinaryUtils;
	
	public String exportPdfBase64(String templatePath, Map<String, Object> data) throws Exception {
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

	
	public String generateWordFromTemplateReturnBase64(String templatePath, Map<String,Object> data) throws IOException, XDocReportException {
		InputStream templateFile = getClass().getResourceAsStream(templatePath);
		if(templateFile == null) throw new FileNotFoundException("Template not found" + templatePath);

		
		IXDocReport report =  XDocReportRegistry.getRegistry().loadReport(templateFile,TemplateEngineKind.Freemarker);
		
		IContext context = report.createContext();
		data.forEach((t, u) -> context.put(t, u));
		File wordFile = File.createTempFile("xdoc", ".docx");
		try(OutputStream out = new FileOutputStream(wordFile)){
			report.process(context, out);
		}
		
		byte[] fileBytes = Files.readAllBytes(wordFile.toPath());
		safeDelete(wordFile);
		
		return Base64.getEncoder().encodeToString(fileBytes);
	}
	
	public File generateWordFromTemplate(String templatePath, Map<String,Object> data) throws IOException, XDocReportException {
		InputStream templateFile = getClass().getResourceAsStream(templatePath);
		if(templateFile == null) throw new FileNotFoundException("Template not found" + templatePath);

		
		IXDocReport report =  XDocReportRegistry.getRegistry().loadReport(templateFile,TemplateEngineKind.Freemarker);
		
		IContext context = report.createContext();
		data.forEach((t, u) -> context.put(t, u));
		File wordFile = File.createTempFile("xdoc", ".docx");
		try(OutputStream out = new FileOutputStream(wordFile)){
			report.process(context, out);
		}
		
		return wordFile;
	}
	
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
    
    public String[] saveImageFromBase64(String base64String, String fileName) {
        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64String);
            Files.write(Paths.get(fileName), imageBytes);
            //System.out.println("Đã lưu ảnh thành công vào file: " + fileName);
            String imgAtt[] = cloudinaryUtils.upload(fileName);
            //System.out.println("đã tải ảnh");
            if(Files.deleteIfExists(Paths.get(fileName)))
            	System.out.println("đã xóa ảnh khỏi local");
           
            return imgAtt;
        } catch (IOException e) {
            System.err.println("Lỗi khi lưu file ảnh: " + e.getMessage());
        }
		return null;
    }

}
