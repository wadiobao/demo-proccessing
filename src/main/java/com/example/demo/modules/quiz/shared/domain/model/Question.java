package com.example.demo.modules.quiz.shared.domain.model;

import org.springframework.data.mongodb.core.index.TextIndexed;

import com.google.gson.annotations.SerializedName;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Domain model representing a single quiz question.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Question {
    @SerializedName("question_number")
    int id;

    @SerializedName("question")
    @TextIndexed
    String question;

    @SerializedName("options")
    Answer answer;

    @SerializedName("image_prompt")
    String imgPrompt;

    @SerializedName("source_reference")
    String reference;

    @SerializedName("bloom_level")
    String bloomLevel;

    @SerializedName("difficulty")
    float difficulty;

    String imgUrl;

    String imgPublicId;

    @SerializedName("bank_id")
    String bankId;
}
