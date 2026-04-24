package com.example.demo.modules.quiz.shared.infrastructure.persistence.entity;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import com.example.demo.dto.basemodel.BaseModel;
import com.example.demo.modules.quiz.shared.domain.model.Question;

import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

/**
 * MongoDB Entity for storing a generated quiz session and its downloadable files.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "archived_questions")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ArchivedQuestionMongoEntity extends BaseModel {
    @Id
    String id;
    String author;
    String title;
    List<Question> questions;
    String pdfBase64;
    String wordBase64;
    String resourceId;
    @Default
    boolean isEvaluated = false;
}
