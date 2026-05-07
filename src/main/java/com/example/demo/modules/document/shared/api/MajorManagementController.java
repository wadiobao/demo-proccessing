package com.example.demo.modules.document.shared.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.document.shared.domain.model.Major;
import com.example.demo.modules.document.shared.infrastructure.persistence.MajorRepository;

/**
 * Controller for administrative management of knowledge fields/majors.
 */
@RestController
@RequestMapping("/api/v1/admin/majors")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class MajorManagementController {

    @Autowired
    private MajorRepository majorRepository;

    /**
     * Retrieves all majors for administrative view.
     */
    @GetMapping
    public ResponseEntity<StateResponse<List<Major>>> getAllMajors() {
        return ResponseEntity.ok(StateResponse.<List<Major>>builder()
                .result(majorRepository.findAll())
                .build());
    }

    /**
     * Creates a new major/field.
     */
    @PostMapping
    public ResponseEntity<StateResponse<Major>> createMajor(@RequestBody Major major) {
        if (majorRepository.findByCode(major.getCode()).isPresent()) {
            throw new RuntimeException("Major code already exists: " + major.getCode());
        }
        return ResponseEntity.ok(StateResponse.<Major>builder()
                .result(majorRepository.save(major))
                .message("Major created successfully")
                .build());
    }

    /**
     * Updates an existing major/field.
     */
    @PutMapping("/{id}")
    public ResponseEntity<StateResponse<Major>> updateMajor(@PathVariable Long id, @RequestBody Major majorDetails) {
        Major major = majorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Major not found with id: " + id));
        major.setDisplayName(majorDetails.getDisplayName());
        major.setCode(majorDetails.getCode());
        return ResponseEntity.ok(StateResponse.<Major>builder()
                .result(majorRepository.save(major))
                .message("Major updated successfully")
                .build());
    }

    /**
     * Deletes a major/field by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<StateResponse<Object>> deleteMajor(@PathVariable Long id) {
        majorRepository.deleteById(id);
        return ResponseEntity.ok(StateResponse.builder()
                .message("Major deleted successfully")
                .build());
    }
}
