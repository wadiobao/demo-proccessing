package com.example.demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.PdfFileFilterRequest;
import com.example.demo.service.iservice.IPdfFileService;
import com.example.demo.sql.entity.PdfFile;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/pdfs")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PdfController {

	
	IPdfFileService pdfFileService;

	@GetMapping
	public List<PdfFile> getAllPdfs() {
		return pdfFileService.getAllPdfs();
	}

	@PostMapping("/delete")
	public ResponseEntity<Boolean> deletePdf(@RequestParam String cloudinaryId) {
		ResponseEntity<Boolean> responseEntity = ResponseEntity.ok().body(pdfFileService.delete(cloudinaryId));
		return responseEntity;
	}
	
	@PostMapping("/delete/checked")
	public ResponseEntity<Boolean> deleteCheckedPdf(@RequestParam List<String> cloudinaryId) {
		ResponseEntity<Boolean> responseEntity = ResponseEntity.ok().body(pdfFileService.deleteChecked(cloudinaryId));
		return responseEntity;
	}
	
	
	@GetMapping("/filter")
	public List<PdfFile> findAllByMajor(@RequestBody PdfFileFilterRequest request) {
		return pdfFileService.findAllByMajor(request);
	}
}
