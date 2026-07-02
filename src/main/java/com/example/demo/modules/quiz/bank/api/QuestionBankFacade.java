package com.example.demo.modules.quiz.bank.api;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.demo.modules.quiz.bank.api.request.QuestionBankRequest;
import com.example.demo.modules.quiz.bank.api.response.QuestionBankResponse;
import com.example.demo.modules.quiz.shared.domain.model.Question;

/**
 * Facade for Question Bank operations (storage, retrieval, committing staged questions).
 */
public interface QuestionBankFacade {
    /**
     * Commits a list of staged questions into the question bank.
     */
    void commitStagedQuestions(List<Question> questions, String username, String contentId);

    /**
     * Retrieves all questions associated with a specific content ID.
     */
    List<Question> getQuestionsByContentId(String contentId);

    /**
     * Finds questions with pagination.
     */
    Page<QuestionBankResponse> findAll(Pageable pageable);

    /**
     * Searches questions by keyword with pagination.
     */
    Page<QuestionBankResponse> search(String keyword, Pageable pageable);

    /**
     * Updates a question in the bank.
     */
    QuestionBankResponse updateQuestion(String id, QuestionBankRequest request, String username);

    /**
     * Deletes a question from the bank.
     */
    void deleteQuestion(String id, String username);
}
