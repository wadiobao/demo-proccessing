
package com.example.demo.sql.controller;

import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.StateResponse;
import com.example.demo.sql.service.iservice.IQuizService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@CrossOrigin(origins = "${app.cors.allowed-origins}")
@RequestMapping("/api")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QuizController {

	
	IQuizService quizService;

	@PostMapping("/handlepdf")
	public StateResponse<Object> handlePdf(@RequestParam MultipartFile file,
			@RequestParam("questionCount")@Min(value = 1) @Max(value = 50) int questionCount,
			@RequestParam("level") @Min(value = 0) @Max(value = 1) int level,
			@RequestParam("type") @Min(value = 0) @Max(value = 1) int type,
			@RequestParam("language") @DefaultValue(value = "vietnamese") String language) throws Exception {
		return quizService.publicHandlePdf(file, questionCount, level,type,language);
	}
	
	@PostMapping("/handlepdf/private")
	public StateResponse<Object> handlePdfPrivate(@RequestParam MultipartFile file,
			@RequestParam("questionCount")@Min(value = 1) @Max(value = 50) int questionCount,
			@RequestParam("level") @Min(value = 0) @Max(value = 1) int level,
			@RequestParam("type") @Min(value = 0) @Max(value = 1) int type,
			@RequestParam("language") @DefaultValue(value = "vietnamese") String language) throws Exception {
		return quizService.privateHandlePdf(file, questionCount, level,type,language);
	}


}