package com.example.demo.modules.quiz.shared.domain.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Data model for a fully generated quiz response, including metadata and base64 files.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class FileGenerateResponse {
    List<Question> questions;
    String wordBase64;
    String pdfBase64;
    String topic;
    String archivedQuestionId;
    @JsonIgnore
    String contentPdf;
}
