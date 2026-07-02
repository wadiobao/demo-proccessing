package com.example.demo.modules.quiz.bank.api.request;

import com.example.demo.enums.VerificationStatus;
import com.example.demo.modules.quiz.shared.domain.model.Question;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Request DTO for updating Question Bank entries.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuestionBankRequest {
    String contentId;
    Question questionData;
    double difficulty;
    VerificationStatus verificationStatus;
    boolean isCommunitySourced;
}
