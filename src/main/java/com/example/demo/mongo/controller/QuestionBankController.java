package com.example.demo.mongo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.StateResponse;
import com.example.demo.mongo.entity.QuestionBank;
import com.example.demo.mongo.service.QuestionBankService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Controller for managing the community-driven Question Bank.
 */
@RestController
@RequestMapping("/api/v1/question-bank")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QuestionBankController {

        QuestionBankService questionBankService;

        /**
         * Updates an existing banked question.
         * Restricted to users with EXPERT tier or higher.
         * Enforces a daily edit quota.
         *
         * @param id          ID of the question to update
         * @param updatedData New question data
         * @return StateResponse containing the updated entry
         */
        @PutMapping("/{id}")
        public ResponseEntity<StateResponse<Object>> updateQuestion(
                        @PathVariable("id") String id,
                        @RequestBody QuestionBank updatedData) {

                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                String username = auth.getName();

                QuestionBank result = questionBankService.updateQuestion(id, updatedData, username);

                return ResponseEntity.ok(StateResponse.builder()
                                .result(result)
                                .message("Question updated successfully")
                                .build());
        }

        /**
         * Search for community questions using MongoDB Text Search.
         */
        @GetMapping("/search")
        public ResponseEntity<StateResponse<Object>> search(
                        @RequestParam("keyword") String keyword,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

                return ResponseEntity.ok(StateResponse.builder()
                                .result(questionBankService.searchQuestions(keyword,
                                                org.springframework.data.domain.PageRequest.of(page, size)))
                                .build());
        }
}
