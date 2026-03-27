package com.example.demo.mongo.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.example.demo.enums.VerificationStatus;
import com.example.demo.mongo.dto.question.Question;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Stores individual questions in a permanent bank.
 * Linked to a specific Content record.
 * Tracks performance and difficulty statistics for IRT calibration.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "question_bank")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuestionBank {
    @Id
    String id;

    @Indexed
    String contentId;

    @Indexed
    String questionHash; // MD5/SHA of question text for deduplication

    @TextIndexed
    Question questionData;

    @Builder.Default
    long attempts = 0;

    @Builder.Default
    long correctCount = 0;

    @Builder.Default
    double difficulty = 0.0; // The 'b' parameter in IRT

    @Indexed
    String contributorId; // ID of the user who uploaded/edited this

    @Builder.Default
    boolean isCommunitySourced = false;

    @Builder.Default
    VerificationStatus verificationStatus =  VerificationStatus.SYSTEM;

    @CreatedDate
    LocalDateTime createdAt;
}
