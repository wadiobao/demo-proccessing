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

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.document.upload.api.request.UploadDocumentRequest;
import com.example.demo.modules.document.upload.application.command.UpdateDocumentCommand;
import com.example.demo.modules.document.upload.application.command.UploadDocumentCommand;
import com.example.demo.modules.document.upload.application.dto.DocumentDto;
import com.example.demo.modules.document.upload.application.mapper.DocumentMapper;
import com.example.demo.modules.document.upload.application.usecase.command.UpdateDocumentUseCase;
import com.example.demo.modules.document.upload.application.usecase.command.UploadDocumentUseCase;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/v1/pdfs") // TODO FIXME: Thay đổi route cẩn thận khi refactor
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UploadDocumentController {

    UploadDocumentUseCase uploadPdfUseCase;
    UpdateDocumentUseCase updatePdfUseCase;
    DocumentMapper pdfFileMapper;

    @PostMapping("/upload")
    public ResponseEntity<StateResponse<Object>> uploadPdf(
            @RequestPart("file") MultipartFile file,
            @RequestPart("request") UploadDocumentRequest request) throws IOException {
        
        UploadDocumentCommand command = UploadDocumentCommand.builder()
                .file(file)
                .request(request)
                .build();
                
        DocumentDto dto = uploadPdfUseCase.execute(command);
        return ResponseEntity.ok(StateResponse.builder().result(pdfFileMapper.toResponse(dto)).build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<StateResponse<Object>> updatePdf(
            @PathVariable("id") Long id,
            @RequestBody UploadDocumentRequest request) {
            
        UpdateDocumentCommand command = UpdateDocumentCommand.builder()
                .id(id)
                .request(request)
                .build();
                
        DocumentDto dto = updatePdfUseCase.execute(command);
        return ResponseEntity.ok(StateResponse.builder().result(pdfFileMapper.toResponse(dto)).build());
    }
}
