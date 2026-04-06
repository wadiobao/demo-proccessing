package com.example.demo.sql.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import com.example.demo.dto.StateResponse;
import com.example.demo.sql.dto.PdfFileFilterRequest;
import com.example.demo.sql.dto.PdfFileRequest;
import com.example.demo.sql.entity.PdfFile;
import com.example.demo.sql.service.iservice.IPdfFileService;

import jakarta.servlet.http.HttpServletResponse;
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

	// @PostMapping("/delete")
	// public ResponseEntity<StateResponse<Object>> deletePdf(@RequestParam String
	// cloudinaryId) {
	// return
	// ResponseEntity.ok(StateResponse.builder().result(pdfFileService.delete(cloudinaryId)).build());
	// }

	// @PostMapping("/delete/checked")
	// public ResponseEntity<StateResponse<Object>> deleteCheckedPdf(@RequestParam
	// List<String> cloudinaryId) {
	// return
	// ResponseEntity.ok(StateResponse.builder().result(pdfFileService.deleteChecked(cloudinaryId)).build());
	// }

	@GetMapping("/filter")
	public ResponseEntity<StateResponse<Object>> findAllByMajor(@RequestBody PdfFileFilterRequest request) {
		return ResponseEntity.ok(StateResponse.builder().result(pdfFileService.findAllByMajor(request)).build());
	}

	@PostMapping("/upload")
	public ResponseEntity<StateResponse<Object>> uploadPdf(
			@RequestPart("file") MultipartFile file,
			@RequestPart("request") PdfFileRequest request) throws java.io.IOException {
		return ResponseEntity.ok(StateResponse.builder().result(pdfFileService.uploadPdf(file, request)).build());
	}

	@PutMapping("/{id}")
	public ResponseEntity<StateResponse<Object>> updatePdf(
			@PathVariable("id") Long id,
			@RequestBody PdfFileRequest request) {
		return ResponseEntity.ok(StateResponse.builder().result(pdfFileService.updatePdf(id, request)).build());
	}

	/**
	 * Streams a PDF document from Cloudinary directly to the response.
	 * 
	 * <p>
	 * Phòng chống SSRF bằng cách sử dụng ID để lấy URL từ database thay vì nhận URL
	 * từ client.
	 * Tối ưu hiệu năng bằng cách sử dụng phương thức transferTo() để truyền dữ
	 * liệu.
	 *
	 * @param id       ID của file PDF trong hệ thống.
	 * @param response Đối tượng HttpServletResponse để ghi dữ liệu.
	 */
	@GetMapping("/{id}/view")
	public void streamPdfFromCloudinary(@PathVariable Long id, HttpServletResponse response) {
		try {
			// 1. Lấy thông tin URL từ Database để tránh SSRF
			PdfFile pdfFile = pdfFileService.getById(id);
			URL url = new URL(pdfFile.getPdfUrl());
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");

			// 2. Thiết lập Header cho trình duyệt (Mở trực tiếp - inline)
			response.setContentType(MediaType.APPLICATION_PDF_VALUE);
			String fileName = pdfFile.getTitle() + ".pdf";
			// Mã hóa tên file để tránh lỗi Unicode
			String encodedFileName = UriUtils.encode(fileName, StandardCharsets.UTF_8);

			response.setHeader(HttpHeaders.CONTENT_DISPOSITION, 
			    "inline; filename*=UTF-8''" + encodedFileName);

			// 3. Streaming dữ liệu (Sử dụng transferTo để tối ưu hiệu năng)
			try (InputStream inputStream = connection.getInputStream();
					OutputStream outputStream = response.getOutputStream()) {
				inputStream.transferTo(outputStream);
				outputStream.flush();
			}
		} catch (IOException e) {
			// Trả về lỗi 404 nếu không tải được file
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}
	}
}
