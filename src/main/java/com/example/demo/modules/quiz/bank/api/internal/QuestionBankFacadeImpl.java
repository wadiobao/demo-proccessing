package com.example.demo.modules.quiz.bank.api.internal;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.demo.modules.quiz.bank.api.QuestionBankFacade;
import com.example.demo.modules.quiz.bank.application.service.BulkQuestionUploadService;
import com.example.demo.modules.quiz.bank.application.usecase.DeleteBankQuestionUseCase;
import com.example.demo.modules.quiz.bank.application.usecase.PromoteBankQuestionsUseCase;
import com.example.demo.modules.quiz.bank.application.usecase.SearchBankQuestionsUseCase;
import com.example.demo.modules.quiz.bank.application.usecase.UpdateBankQuestionUseCase;
import com.example.demo.modules.quiz.shared.domain.model.Question;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.QuestionBankMongoEntity;

import lombok.RequiredArgsConstructor;

/**
 * Implementation of QuestionBankFacade.
 */
@Service
@RequiredArgsConstructor
class QuestionBankFacadeImpl implements QuestionBankFacade {

    private final PromoteBankQuestionsUseCase promoteUseCase;
    private final SearchBankQuestionsUseCase searchUseCase;
    private final UpdateBankQuestionUseCase updateUseCase;
    private final DeleteBankQuestionUseCase deleteUseCase;
    private final BulkQuestionUploadService bulkUploadService;

    @Override
    public void commitStagedQuestions(List<Question> questions, String username, String contentId) {
        bulkUploadService.commitStagedQuestions(questions, username, contentId);
    }

    @Override
    public List<Question> getQuestionsByContentId(String contentId) {
        return searchUseCase.getQuestionsByContentId(contentId);
    }

    @Override
    public Page<QuestionBankMongoEntity> findAll(Pageable pageable) {
        return searchUseCase.findAll(pageable);
    }

    @Override
    public Page<QuestionBankMongoEntity> search(String keyword, Pageable pageable) {
        return searchUseCase.search(keyword, pageable);
    }

    @Override
    public QuestionBankMongoEntity updateQuestion(String id, QuestionBankMongoEntity updatedData, String username) {
        return updateUseCase.execute(id, updatedData, username);
    }

    @Override
    public void deleteQuestion(String id, String username) {
        deleteUseCase.execute(id, username);
    }
}
