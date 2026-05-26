package com.example.demo.modules.quiz.adaptive.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TaskStatus {
	private String status;
	private String contentHash;

}
