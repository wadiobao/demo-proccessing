package com.example.demo.modules.quiz.bank.api.response;

import java.time.LocalDateTime;

import com.example.demo.enums.VerificationStatus;
import com.example.demo.modules.quiz.shared.domain.model.Question;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Response DTO for Question Bank entries.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuestionBankResponse {
    String id;
    String contentId;
    String questionHash;
    Question questionData;
    long attempts;
    long correctCount;
    double difficulty;
    String contributorId;
    boolean isCommunitySourced;
    VerificationStatus verificationStatus;
    LocalDateTime createdAt;
}
