package com.example.demo.modules.quiz.bank.api.internal;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.demo.modules.quiz.bank.api.QuestionBankFacade;
import com.example.demo.modules.quiz.bank.api.mapper.QuestionBankMapper;
import com.example.demo.modules.quiz.bank.api.request.QuestionBankRequest;
import com.example.demo.modules.quiz.bank.api.response.QuestionBankResponse;
import com.example.demo.modules.quiz.bank.application.service.BulkQuestionUploadService;
import com.example.demo.modules.quiz.bank.application.usecase.DeleteBankQuestionUseCase;
import com.example.demo.modules.quiz.bank.application.usecase.PromoteBankQuestionsUseCase;
import com.example.demo.modules.quiz.bank.application.usecase.SearchBankQuestionsUseCase;
import com.example.demo.modules.quiz.bank.application.usecase.UpdateBankQuestionUseCase;
import com.example.demo.modules.quiz.shared.domain.model.Question;

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
    private final QuestionBankMapper bankMapper;

    @Override
    public void commitStagedQuestions(List<Question> questions, String username, String contentId) {
        bulkUploadService.commitStagedQuestions(questions, username, contentId);
    }

    @Override
    public List<Question> getQuestionsByContentId(String contentId) {
        return searchUseCase.getQuestionsByContentId(contentId);
    }

    @Override
    public Page<QuestionBankResponse> findAll(Pageable pageable) {
        return bankMapper.toResponsePage(searchUseCase.findAll(pageable));
    }

    @Override
    public Page<QuestionBankResponse> search(String keyword, Pageable pageable) {
        return bankMapper.toResponsePage(searchUseCase.search(keyword, pageable));
    }

    @Override
    public QuestionBankResponse updateQuestion(String id, QuestionBankRequest request, String username) {
        return bankMapper.toResponse(updateUseCase.execute(id, bankMapper.toEntity(request), username));
    }

    @Override
    public void deleteQuestion(String id, String username) {
        deleteUseCase.execute(id, username);
    }
}
