package com.example.demo.modules.quiz.shared.domain.model;

import com.google.gson.annotations.SerializedName;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Domain model representing multiple-choice answers for a question.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Answer {
    @SerializedName("A")
    String option1;

    @SerializedName("B")
    String option2;

    @SerializedName("C")
    String option3;

    @SerializedName("D")
    String option4;

    @SerializedName("correct_answer")
    String correctAnswer;

    @SerializedName("explanation")
    String explanation;
}
