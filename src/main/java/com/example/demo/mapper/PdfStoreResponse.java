package com.example.demo.mapper;

import java.time.LocalDateTime;
import java.util.List;

import com.example.demo.mongo.dto.question.Question;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PdfStoreResponse {
    String author;
    String title;
    List<Question> questions;
    LocalDateTime createdAt;
    String pdfBase64;
    String wordBase64;
}
