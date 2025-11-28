package com.example.demo.mongo.entity;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import com.example.demo.dto.basemodel.BaseModel;
import com.example.demo.dto.question.Question;

import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "archived_questions")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ArchivedQuestion extends BaseModel {
    @Id
    String id;
    String author;
    String title;
    List<Question> content;
    String pdfBase64;
    String wordBase64;
    String resourceId;
}
