package com.example.demo.sql.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.StateResponse;
import com.example.demo.sql.entity.Major;
import com.example.demo.sql.repository.MajorRepository;

/**
 * Controller for public access to knowledge fields/majors.
 */
@RestController
@RequestMapping("/api/v1/majors")
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class MajorController {

    @Autowired
    private MajorRepository majorRepository;

    /**
     * Retrieves all available knowledge fields/majors.
     */
    @GetMapping
    public ResponseEntity<StateResponse<List<Major>>> getAllMajors() {
        return ResponseEntity.ok(StateResponse.<List<Major>>builder()
                .result(majorRepository.findAll())
                .build());
    }
}
