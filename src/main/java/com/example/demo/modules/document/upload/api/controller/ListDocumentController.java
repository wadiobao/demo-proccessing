package com.example.demo.modules.document.upload.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.document.upload.api.request.ListDocumentQueryRequest;
import com.example.demo.modules.document.upload.application.mapper.DocumentMapper;
import com.example.demo.modules.document.upload.application.query.FilterDocumentQuery;
import com.example.demo.modules.document.upload.application.query.ListDocumentQuery;
import com.example.demo.modules.document.upload.application.usecase.query.ListDocumentUseCase;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/v1/pdfs") // TODO FIXME: Cẩn thận duplicate mapping nếu không gom facade
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ListDocumentController {

    ListDocumentUseCase listPdfUseCase;
    DocumentMapper pdfFileMapper;

    @GetMapping
    public ResponseEntity<StateResponse<Object>> getAllPdfs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
            
        ListDocumentQuery query = ListDocumentQuery.builder()
                .page(page)
                .size(size)
                .build();
                
        return ResponseEntity
                .ok(StateResponse.builder().result(listPdfUseCase.executeAll(query).map(pdfFileMapper::toResponse)).build());
    }

    @GetMapping("/filter")
    public ResponseEntity<StateResponse<Object>> findAllByMajor(@RequestBody ListDocumentQueryRequest request) {
        FilterDocumentQuery query = FilterDocumentQuery.builder()
                .request(request)
                .build();
                
        return ResponseEntity.ok(StateResponse.builder().result(listPdfUseCase.executeFilter(query).map(pdfFileMapper::toResponse)).build());
    }
}
