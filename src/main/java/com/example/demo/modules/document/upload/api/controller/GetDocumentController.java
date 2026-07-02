package com.example.demo.modules.document.upload.api.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

import com.example.demo.modules.document.upload.application.dto.DocumentDto;
import com.example.demo.modules.document.upload.application.query.GetDocumentQuery;
import com.example.demo.modules.document.upload.application.usecase.query.GetDocumentUseCase;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/v1/pdfs") 
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GetDocumentController {

    GetDocumentUseCase getPdfUseCase;

    @GetMapping("/{id}/view")
    public void streamPdfFromCloudinary(@PathVariable Long id, HttpServletResponse response) {
        try {
            GetDocumentQuery query = GetDocumentQuery.builder().id(id).build();
            DocumentDto pdfFileDto = getPdfUseCase.execute(query);
            URL url = new URL(pdfFileDto.getPdfUrl());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            response.setContentType(MediaType.APPLICATION_PDF_VALUE);
            String fileName = pdfFileDto.getTitle() + ".pdf";
            String encodedFileName = UriUtils.encode(fileName, StandardCharsets.UTF_8);

            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, 
                "inline; filename*=UTF-8''" + encodedFileName);

            try (InputStream inputStream = connection.getInputStream();
                    OutputStream outputStream = response.getOutputStream()) {
                inputStream.transferTo(outputStream);
                outputStream.flush();
            }
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
