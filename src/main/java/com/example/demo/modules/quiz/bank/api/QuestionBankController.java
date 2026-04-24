package com.example.demo.modules.quiz.bank.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.quiz.bank.application.SearchBankQuestionsUseCase;
import com.example.demo.modules.quiz.bank.application.UpdateBankQuestionUseCase;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.QuestionBankMongoEntity;

import lombok.RequiredArgsConstructor;

/**
 * REST Controller for community-driven Question Bank operations.
 */
@RestController
@RequestMapping("/api/v1/quiz/bank")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class QuestionBankController {

    private final SearchBankQuestionsUseCase searchUseCase;
    private final UpdateBankQuestionUseCase updateUseCase;

    /**
     * Searches for questions in the bank.
     */
    @GetMapping("/search")
    public ResponseEntity<StateResponse<Object>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<QuestionBankMongoEntity> result = searchUseCase.search(keyword, PageRequest.of(page, size));
        return ResponseEntity.ok(StateResponse.builder().result(result).build());
    }

    /**
     * Updates a question (requires Expert/Moderator reputation).
     */
    @PutMapping("/{id}")
    public ResponseEntity<StateResponse<Object>> update(
            @PathVariable String id,
            @RequestBody QuestionBankMongoEntity updatedData,
            Authentication authentication) {
        
        String username = authentication.getName();
        QuestionBankMongoEntity saved = updateUseCase.execute(id, updatedData, username);
        
        return ResponseEntity.ok(StateResponse.builder()
                .message("Question updated successfully.")
                .result(saved)
                .build());
    }
}
