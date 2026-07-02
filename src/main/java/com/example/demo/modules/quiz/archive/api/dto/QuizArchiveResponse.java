package com.example.demo.modules.quiz.archive.api.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.demo.modules.quiz.shared.domain.model.Question;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Response DTO for archived quiz records.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuizArchiveResponse {
    String id;
    String author;
    String title;
    List<Question> questions;
    LocalDateTime createdAt;
    String pdfBase64;
    String wordBase64;
    String excelBase64;
}
