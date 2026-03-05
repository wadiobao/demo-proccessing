package com.example.demo.mongo.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.enums.Role;
import com.example.demo.mongo.dto.question.Answer;
import com.example.demo.mongo.dto.question.Question;
import com.example.demo.mongo.entity.QuestionBank;
import com.example.demo.mongo.repository.QuestionBankRepository;
import com.example.demo.sql.entity.User;
import com.example.demo.sql.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for bulk ingestion of community-contributed questions from Excel.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class BulkQuestionUploadService {

    QuestionBankRepository questionBankRepository;
    UserRepository userRepository;

    /**
     * Processes an Excel file and saves questions to the bank if the user has
     * sufficient reputation.
     */
    public List<QuestionBank> uploadQuestionsFromExcel(MultipartFile file, String username, String topic)
            throws Exception {
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getCurrentTier() == Role.RESTRICTED) {
            throw new RuntimeException("User is restricted from uploading content due to low reputation.");
        }

        List<QuestionBank> savedQuestions = new ArrayList<>();
        try (InputStream is = file.getInputStream();
                Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0); // Assume first sheet
            for (Row row : sheet) {
                if (row.getRowNum() == 0)
                    continue; // Skip header

                try {
                    Question question = parseRowToQuestion(row);
                    QuestionBank bankEntry = QuestionBank.builder()
                            .contributorId(username)
                            .isCommunitySourced(true)
                            .verificationStatus("COMMUNITY")
                            .questionData(question)
                            .difficulty(0.0) // Initial neutral difficulty
                            .build();

                    savedQuestions.add(questionBankRepository.save(bankEntry));
                } catch (Exception e) {
                    log.error("Error parsing row {}: {}", row.getRowNum(), e.getMessage());
                }
            }
        }

        log.info("Bulk uploaded {} questions for user: {}", savedQuestions.size(), username);
        return savedQuestions;
    }

    private Question parseRowToQuestion(Row row) {
        String content = row.getCell(0).getStringCellValue();
        String ansA = row.getCell(1).getStringCellValue();
        String ansB = row.getCell(2).getStringCellValue();
        String ansC = row.getCell(3).getStringCellValue();
        String ansD = row.getCell(4).getStringCellValue();
        String correct = row.getCell(5).getStringCellValue(); // Expecting A, B, C, or D

        // Optional explanation column (Column G / Index 6)
        String explanation = "Community contributed question";
        if (row.getCell(6) != null) {
            explanation = row.getCell(6).getStringCellValue();
        }

        Answer answer = Answer.builder()
                .A(ansA)
                .B(ansB)
                .C(ansC)
                .D(ansD)
                .correct(correct.toUpperCase())
                .explain(explanation)
                .build();

        return Question.builder()
                .question(content)
                .answer(answer)
                .bloomLevel("Understanding") // Default
                .build();
    }
}
