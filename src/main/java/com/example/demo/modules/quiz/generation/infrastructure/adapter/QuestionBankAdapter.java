package com.example.demo.modules.quiz.generation.infrastructure.adapter;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.example.demo.modules.quiz.generation.application.port.QuestionBankPort;
import com.example.demo.modules.quiz.shared.domain.model.Question;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.repository.QuestionBankRepository;

import lombok.RequiredArgsConstructor;

/**
 * Adapter implementing the QuestionBankPort using MongoDB persistence.
 */
@Component
@RequiredArgsConstructor
public class QuestionBankAdapter implements QuestionBankPort {

    private final QuestionBankRepository repository;

    @Override
    public long countByContentId(String contentId) {
        return repository.countByContentId(contentId);
    }

    @Override
    public List<Question> getRandomQuestions(String contentId, int count) {
        return repository.findRandomByContentId(contentId, count).stream()
                .map(entity -> entity.getQuestionData())
                .collect(Collectors.toList());
    }
}
