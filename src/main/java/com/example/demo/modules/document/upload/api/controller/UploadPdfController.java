package com.example.demo.modules.document.upload.api.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.modules.document.upload.application.command.UploadPdfCommand;
import com.example.demo.modules.document.upload.application.command.UpdatePdfCommand;
import com.example.demo.modules.document.upload.application.usecase.command.UploadPdfUseCase;
import com.example.demo.modules.document.upload.application.usecase.command.UpdatePdfUseCase;
import com.example.demo.modules.document.upload.api.request.PdfFileRequest;
import com.example.demo.modules.document.upload.application.dto.PdfFileDto;
import com.example.demo.modules.document.upload.application.mapper.PdfFileMapper;
import com.example.demo.dto.StateResponse;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

// TODO FIXME: Đổi tên thành UploadDocumentController
@RestController
@RequestMapping("/api/v1/pdfs") // TODO FIXME: Thay đổi route cẩn thận khi refactor
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UploadPdfController {

    UploadPdfUseCase uploadPdfUseCase;
    UpdatePdfUseCase updatePdfUseCase;
    PdfFileMapper pdfFileMapper;

    @PostMapping("/upload")
    public ResponseEntity<StateResponse<Object>> uploadPdf(
            @RequestPart("file") MultipartFile file,
            @RequestPart("request") PdfFileRequest request) throws IOException {
        
        UploadPdfCommand command = UploadPdfCommand.builder()
                .file(file)
                .request(request)
                .build();
                
        PdfFileDto dto = uploadPdfUseCase.execute(command);
        return ResponseEntity.ok(StateResponse.builder().result(pdfFileMapper.toResponse(dto)).build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<StateResponse<Object>> updatePdf(
            @PathVariable("id") Long id,
            @RequestBody PdfFileRequest request) {
            
        UpdatePdfCommand command = UpdatePdfCommand.builder()
                .id(id)
                .request(request)
                .build();
                
        PdfFileDto dto = updatePdfUseCase.execute(command);
        return ResponseEntity.ok(StateResponse.builder().result(pdfFileMapper.toResponse(dto)).build());
    }
}
