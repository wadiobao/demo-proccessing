package com.example.demo.modules.document.processing.infrastructure.adapter.excel;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.modules.document.processing.application.port.output.TextExtractorPort;

import lombok.extern.slf4j.Slf4j;

/**
 * Adapter for extracting text from Microsoft Excel (.xlsx) files using Apache POI.
 */
@Component
@Slf4j
public class PoiExcelAdapter implements TextExtractorPort {

    @Override
    public boolean supports(String extension) {
        return "xlsx".equalsIgnoreCase(extension);
    }

    @Override
    public String extractText(MultipartFile file) throws IOException {
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
            return text.toString();
        } catch (Exception e) {
            log.error("Error processing Excel file: {}", e.getMessage());
            throw new IOException("Failed to process Excel file: " + e.getMessage(), e);
        }
    }
}
