package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.dto.StateResponse;
import com.example.demo.service.iservice.IFileGenerateService123;

public class FileGenerateController {
	IFileGenerateService123 fileGenerateService123;
	
	@GetMapping("/test")
	public StateResponse<Object> generateWord(){
		return fileGenerateService123.generateWordAndPdfBase64();
	}
}

