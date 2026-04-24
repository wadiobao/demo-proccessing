package com.example.demo.modules.quiz.shared.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Domain model representing a user's answer history for difficulty calibration.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserAnswer {
    String id;
    String bankId;
    boolean isTrue;
    double difficulty;
    String bloomLevel;
}
