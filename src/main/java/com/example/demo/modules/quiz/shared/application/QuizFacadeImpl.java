package com.example.demo.modules.quiz.shared.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.quiz.adaptive.api.AdaptiveQuizFacade;
import com.example.demo.modules.quiz.adaptive.api.request.QuizSubmissionRequest;
import com.example.demo.modules.quiz.bank.api.QuestionBankFacade;
import com.example.demo.modules.quiz.public_quiz.api.PublicQuizFacade;
import com.example.demo.modules.quiz.shared.api.QuizFacade;
import com.example.demo.modules.quiz.shared.domain.model.Question;
import com.example.demo.modules.quiz.shared.domain.model.QuizConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the QuizFacade that delegates to specialized facades.
 * Maintains compatibility for external modules (Community, Admin).
 */
@Service
@RequiredArgsConstructor
@Slf4j
class QuizFacadeImpl implements QuizFacade {

    private final PublicQuizFacade publicQuizFacade;
    private final AdaptiveQuizFacade adaptiveQuizFacade;
    private final QuestionBankFacade questionBankFacade;

    @Override
    public StateResponse<Object> generateQuiz(MultipartFile file, QuizConfig config) {
        return publicQuizFacade.generateQuiz(file, config);
    }

    @Override
    public StateResponse<Object> generatePrivateQuiz(List<MultipartFile> files, QuizConfig config, String username) throws Exception {
        return adaptiveQuizFacade.generatePrivateQuiz(files, config, username);
    }

    @Override
    public StateResponse<Object> submitQuiz(QuizSubmissionRequest request, String username) {
        return adaptiveQuizFacade.submitQuiz(request, username);
    }

    @Override
    public StateResponse<Object> getUserStats(String username, String topic) {
        return adaptiveQuizFacade.getUserStats(username, topic);
    }

    @Override
    public StateResponse<Object> getOverviewStats(String username) {
        return adaptiveQuizFacade.getOverviewStats(username);
    }

    @Override
    public void commitStagedQuestions(List<Question> questions, String username, String contentId) {
        questionBankFacade.commitStagedQuestions(questions, username, contentId);
    }

    @Override
    public List<Question> getQuestionsByContentId(String contentId) {
        return questionBankFacade.getQuestionsByContentId(contentId);
    }
}
