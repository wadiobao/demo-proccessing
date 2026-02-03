package com.example.demo.sql.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.data.domain.PageRequest;
import com.example.demo.dto.StateResponse;

import com.example.demo.sql.dto.PdfFileFilterRequest;
import com.example.demo.sql.service.iservice.IPdfFileService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/v1/pdfs")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PdfController {

	IPdfFileService pdfFileService;

	@GetMapping
	public ResponseEntity<StateResponse<Object>> getAllPdfs(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return ResponseEntity
				.ok(StateResponse.builder().result(pdfFileService.getAllPdfs(PageRequest.of(page, size))).build());
	}

	@PostMapping("/delete")
	public ResponseEntity<StateResponse<Object>> deletePdf(@RequestParam String cloudinaryId) {
		return ResponseEntity.ok(StateResponse.builder().result(pdfFileService.delete(cloudinaryId)).build());
	}

	@PostMapping("/delete/checked")
	public ResponseEntity<StateResponse<Object>> deleteCheckedPdf(@RequestParam List<String> cloudinaryId) {
		return ResponseEntity.ok(StateResponse.builder().result(pdfFileService.deleteChecked(cloudinaryId)).build());
	}

	@GetMapping("/filter")
	public ResponseEntity<StateResponse<Object>> findAllByMajor(@RequestBody PdfFileFilterRequest request) {
		return ResponseEntity.ok(StateResponse.builder().result(pdfFileService.findAllByMajor(request)).build());
	}
}
