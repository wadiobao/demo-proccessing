package com.example.demo.modules.document.upload.api.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.document.upload.application.command.DeleteDocumentCommand;
import com.example.demo.modules.document.upload.application.query.ListDocumentQuery;
import com.example.demo.modules.document.upload.application.usecase.command.DeleteDocumentUseCase;
import com.example.demo.modules.document.upload.application.usecase.query.ListDocumentUseCase;

import lombok.RequiredArgsConstructor;

/**
 * Controller for Administrative file management (PDFs).
 */
@RestController
@RequestMapping("/api/v1/admin/files")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
@PreAuthorize("hasRole('ADMIN')")
public class AdminFileController {

    private final ListDocumentUseCase listPdfUseCase;
    private final DeleteDocumentUseCase deletePdfUseCase;

    /**
     * Lists all PDF files with pagination for management.
     */
    @GetMapping
    public ResponseEntity<StateResponse<Object>> getAllFiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        ListDocumentQuery query = ListDocumentQuery.builder()
                .page(page)
                .size(size)
                .build();
        
        return ResponseEntity.ok(StateResponse.builder()
                .result(listPdfUseCase.executeAll(query))
                .build());
    }

    /**
     * Deletes a single PDF by its Cloudinary ID.
     */
    @DeleteMapping("/{cloudinaryId}")
    public ResponseEntity<StateResponse<Object>> deleteFile(@PathVariable String cloudinaryId) {
        DeleteDocumentCommand command = DeleteDocumentCommand.builder()
                .cloudinaryIds(Collections.singletonList(cloudinaryId))
                .build();
        
        boolean success = deletePdfUseCase.execute(command);
        if (success) {
            return ResponseEntity.ok(StateResponse.builder().message("File deleted successfully").build());
        } else {
            return ResponseEntity.internalServerError().body(StateResponse.builder().message("Failed to delete file").build());
        }
    }

    /**
     * Deletes multiple PDFs by their Cloudinary IDs.
     */
    @DeleteMapping("/bulk")
    public ResponseEntity<StateResponse<Object>> deleteFilesBulk(@RequestBody List<String> cloudinaryIds) {
        DeleteDocumentCommand command = DeleteDocumentCommand.builder()
                .cloudinaryIds(cloudinaryIds)
                .build();
        
        boolean success = deletePdfUseCase.execute(command);
        if (success) {
            return ResponseEntity.ok(StateResponse.builder().message("Files deleted successfully").build());
        } else {
            return ResponseEntity.internalServerError().body(StateResponse.builder().message("Failed to delete some files").build());
        }
    }
}
