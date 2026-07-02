package com.example.demo.modules.document.retrieval.application.usecase;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.example.demo.modules.quiz.shared.domain.model.Question;

import lombok.extern.slf4j.Slf4j;

/**
 * Handles formatting and exporting generated quiz questions to Excel format.
 *
 * <p>Uses Apache POI to compile list of question objects containing answers,
 * explanations, and metadata into a base64 encoded Excel byte stream.
 *
 * @since 1.0.0
 */
@Service
@Slf4j
public class GenerateExcelQuizUseCase {

    /**
     * Transforms questions to an Excel workbook and returns the Base64 representation.
     *
     * @param questions list of questions containing text, options, explanations, Bloom's level, and reference source
     * @return base64 string of the Excel document, or null if an error occurs
     */
    public String execute(List<Question> questions) {
        if (questions == null || questions.isEmpty()) {
            return null;
        }

        // Using standard XSSFWorkbook as quizzes typically have fewer than 100 questions,
        // allowing safe auto-fit columns.
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Quiz Questions");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            String[] headers = {
                "Mã câu hỏi", 
                "Nội dung câu hỏi", 
                "Lựa chọn A", 
                "Lựa chọn B", 
                "Lựa chọn C", 
                "Lựa chọn D", 
                "Đáp án đúng", 
                "Giải thích", 
                "Cấp độ Bloom", 
                "Nguồn tham khảo"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Question q : questions) {
                if (q == null) {
                    continue;
                }
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(q.getId());
                row.createCell(1).setCellValue(q.getQuestion() != null ? q.getQuestion() : "");

                if (q.getAnswer() != null) {
                    row.createCell(2).setCellValue(q.getAnswer().getOption1() != null ? q.getAnswer().getOption1() : "");
                    row.createCell(3).setCellValue(q.getAnswer().getOption2() != null ? q.getAnswer().getOption2() : "");
                    row.createCell(4).setCellValue(q.getAnswer().getOption3() != null ? q.getAnswer().getOption3() : "");
                    row.createCell(5).setCellValue(q.getAnswer().getOption4() != null ? q.getAnswer().getOption4() : "");
                    row.createCell(6).setCellValue(q.getAnswer().getCorrectAnswer() != null ? q.getAnswer().getCorrectAnswer() : "");
                    row.createCell(7).setCellValue(q.getAnswer().getExplanation() != null ? q.getAnswer().getExplanation() : "");
                } else {
                    for (int i = 2; i <= 7; i++) {
                        row.createCell(i).setCellValue("");
                    }
                }

                row.createCell(8).setCellValue(q.getBloomLevel() != null ? q.getBloomLevel() : "");
                row.createCell(9).setCellValue(q.getReference() != null ? q.getReference() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(bos);
            return Base64.getEncoder().encodeToString(bos.toByteArray());

        } catch (Exception e) {
            log.error("Failed to generate Excel quiz document: {}", e.getMessage(), e);
            return null;
        }
    }
}
