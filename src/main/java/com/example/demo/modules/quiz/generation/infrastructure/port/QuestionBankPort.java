package com.example.demo.modules.quiz.generation.infrastructure.port;

import java.util.List;
import com.example.demo.modules.quiz.shared.domain.model.Question;

/**
 * Port for accessing the Question Bank within the Generation module.
 */
public interface QuestionBankPort {
    /**
     * Counts questions available for a specific content.
     */
    long countByContentId(String contentId);

    /**
     * Retrieves a random selection of questions for a specific content.
     */
    List<Question> getRandomQuestions(String contentId, int count);
}
