package com.example.demo.modules.document.upload.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.document.upload.application.query.ListPdfQuery;
import com.example.demo.modules.document.upload.application.query.FilterPdfQuery;
import com.example.demo.modules.document.upload.application.usecase.query.ListPdfUseCase;
import com.example.demo.modules.document.upload.api.request.PdfFileFilterRequest;
import com.example.demo.modules.document.upload.application.mapper.PdfFileMapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

// TODO FIXME: Đổi tên thành ListDocumentController
@RestController
@RequestMapping("/api/v1/pdfs") // TODO FIXME: Cẩn thận duplicate mapping nếu không gom facade
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ListPdfController {

    ListPdfUseCase listPdfUseCase;
    PdfFileMapper pdfFileMapper;

    @GetMapping
    public ResponseEntity<StateResponse<Object>> getAllPdfs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
            
        ListPdfQuery query = ListPdfQuery.builder()
                .page(page)
                .size(size)
                .build();
                
        return ResponseEntity
                .ok(StateResponse.builder().result(listPdfUseCase.executeAll(query).map(pdfFileMapper::toResponse)).build());
    }

    @GetMapping("/filter")
    public ResponseEntity<StateResponse<Object>> findAllByMajor(@RequestBody PdfFileFilterRequest request) {
        FilterPdfQuery query = FilterPdfQuery.builder()
                .request(request)
                .build();
                
        return ResponseEntity.ok(StateResponse.builder().result(listPdfUseCase.executeFilter(query).map(pdfFileMapper::toResponse)).build());
    }
}
