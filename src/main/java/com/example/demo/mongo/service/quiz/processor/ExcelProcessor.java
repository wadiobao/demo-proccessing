package com.example.demo.mongo.service.quiz.processor;

import java.io.InputStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

/**
 * Processor for Microsoft Excel (.xlsx) files.
 * Extracts all text content from all sheets for document analysis.
 */
@Component
@Slf4j
public class ExcelProcessor implements IDocumentProcessor {

    private static final String EXCEL_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    @Override
    public boolean supports(String contentType) {
        return EXCEL_TYPE.equalsIgnoreCase(contentType);
    }

    @Override
    public String extractText(MultipartFile file) throws Exception {
        log.info("Extracting text from Excel: {}", file.getOriginalFilename());
        StringBuilder text = new StringBuilder();

        try (InputStream is = file.getInputStream();
                Workbook workbook = new XSSFWorkbook(is)) {

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                text.append("--- Sheet: ").append(sheet.getSheetName()).append(" ---\n");

                for (Row row : sheet) {
                    for (Cell cell : row) {
                        switch (cell.getCellType()) {
                            case STRING:
                                text.append(cell.getStringCellValue()).append(" ");
                                break;
                            case NUMERIC:
                                text.append(cell.getNumericCellValue()).append(" ");
                                break;
                            case BOOLEAN:
                                text.append(cell.getBooleanCellValue()).append(" ");
                                break;
                            case FORMULA:
                                text.append(cell.getCellFormula()).append(" ");
                                break;
                            default:
                                break;
                        }
                    }
                    text.append("\n");
                }
            }
        } catch (Exception e) {
            log.error("Error processing Excel file: {}", e.getMessage());
            throw new Exception("Failed to process Excel file: " + e.getMessage());
        }

        return text.toString();
    }
}
