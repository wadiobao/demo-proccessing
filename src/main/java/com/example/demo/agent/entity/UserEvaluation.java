package com.example.demo.agent.entity;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.persistence.Id;
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
@Document(collection = "user_evaluation")
public class UserEvaluation {
	@Id
	String id;
	String email;
	UserAnswer answer;
	String confidenceLevel;
	String learningLevel;
	List<String> conceptTags;
	double averageScore;
	double averageTimeSeconds;
	double averageConfidence;
}
