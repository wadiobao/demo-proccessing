package com.example.demo.modules.quiz.shared.domain.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Snapshot of the user's IRT theta score captured after each quiz submission.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ThetaSnapshot {
    // theta of that session
    double theta;
    // accuracy percentage of that session
    double accuracy;
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    LocalDateTime recordedAt;
}
