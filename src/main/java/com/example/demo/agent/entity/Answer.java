package com.example.demo.agent.entity;

import java.util.List;

import com.example.demo.enums.QuestionType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Answer {
	int questionId;
	String question;
	String topic;
	QuestionType questionType;
	List<String> answerOptions;
	String userResponse;
	String correctAnswer;
	boolean isCorrect;
	int timeSpentExpectedSeconds;
	int timeSpentSeconds;
	int questionLevel;
	List<String> conceptTags;
}
