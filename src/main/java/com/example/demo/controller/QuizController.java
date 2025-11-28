package com.example.demo.controller;

import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.agent.entity.UserAnswer;
import com.example.demo.agent.service.GeneralService;
import com.example.demo.dto.StateResponse;
import com.example.demo.service.iservice.IQuizService;

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
	GeneralService generalService;

	@PostMapping("/handlepdf")
	public StateResponse<Object> handlePdf(@RequestParam MultipartFile file,
			@RequestParam("questionCount")@Min(value = 1) @Max(value = 50) int questionCount,
			@RequestParam("mode") @Min(value = 0) @Max(value = 1) int mode,
			@RequestParam("type") @Min(value = 0) @Max(value = 1) int type,
			@RequestParam("language") @DefaultValue(value = "vietnamese") String language) throws Exception {
		return quizService.publicHandlePdf(file, questionCount, mode,type,language);
	}
	
	@PostMapping("/handlepdf/private")
	public StateResponse<Object> handlePdfPrivate(@RequestParam MultipartFile file,
			@RequestParam("questionCount")@Min(value = 1) @Max(value = 50) int questionCount,
			@RequestParam("mode") @Min(value = 0) @Max(value = 1) int mode,
			@RequestParam("type") @Min(value = 0) @Max(value = 1) int type,
			@RequestParam("language") @DefaultValue(value = "vietnamese") String language) throws Exception {
		return quizService.privateHandlePdf(file, questionCount, mode,type,language);
	}

	@PostMapping("/test")
	public StateResponse<Object> test(@RequestBody UserAnswer answer) {
		return generalService.saveAnalysis(answer,answer.getEmail());
	}
	
	@PostMapping("/test/all")
	public StateResponse<Object> gellAllTest(@RequestParam String email) {
		return StateResponse.builder().result(generalService.getUserEvaluationByEmail(email)).build(); 
	}

}
