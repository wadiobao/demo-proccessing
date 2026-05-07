package com.example.demo.modules.quiz.adaptive.api.internal;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.quiz.adaptive.api.AdaptiveQuizFacade;
import com.example.demo.modules.quiz.adaptive.api.request.QuizSubmissionRequest;
import com.example.demo.modules.quiz.adaptive.application.command.AddFileToTopicCommand;
import com.example.demo.modules.quiz.adaptive.application.command.CreateTopicCommand;
import com.example.demo.modules.quiz.adaptive.application.command.DeleteTopicCommand;
import com.example.demo.modules.quiz.adaptive.application.command.GenerateAdaptiveQuizCommand;
import com.example.demo.modules.quiz.adaptive.application.command.GenerateReviewQuizCommand;
import com.example.demo.modules.quiz.adaptive.application.command.UpdateTopicCommand;
import com.example.demo.modules.quiz.adaptive.application.query.GetTopicFilesQuery;
import com.example.demo.modules.quiz.adaptive.application.query.GetTopicInfoQuery;
import com.example.demo.modules.quiz.adaptive.application.query.GetTopicScoreHistoryQuery;
import com.example.demo.modules.quiz.analytics.api.AnalyticsFacade;
import com.example.demo.modules.quiz.evaluation.api.EvaluationFacade;
import com.example.demo.modules.quiz.shared.domain.model.QuizConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of AdaptiveQuizFacade following CQRS principles.
 * Orchestrates interaction between Commands (state changes) and Queries (data retrieval).
 */
@Service
@RequiredArgsConstructor
@Slf4j
class AdaptiveQuizFacadeImpl implements AdaptiveQuizFacade {

    private final EvaluationFacade evaluationFacade;
    private final AnalyticsFacade analyticsFacade;
    
    // Commands
    private final GenerateAdaptiveQuizCommand generateAdaptiveQuizCommand;
    private final GenerateReviewQuizCommand generateReviewQuizCommand;
    private final CreateTopicCommand createTopicCommand;
    private final UpdateTopicCommand updateTopicCommand;
    private final DeleteTopicCommand deleteTopicCommand;
    private final AddFileToTopicCommand addFileToTopicCommand;

    // Queries
    private final GetTopicFilesQuery getTopicFilesQuery;
    private final GetTopicInfoQuery getTopicInfoQuery;
    private final GetTopicScoreHistoryQuery getTopicScoreHistoryQuery;

    @Override
    public StateResponse<Object> generatePrivateQuiz(List<MultipartFile> files, QuizConfig config, String username) throws Exception {
        return generateAdaptiveQuizCommand.execute(files, config, username);
    }

    @Override
    public StateResponse<Object> generateReviewQuiz(String topicId, QuizConfig config, String username) throws Exception {
        return generateReviewQuizCommand.execute(topicId, config, username);
    }

    @Override
    public StateResponse<Object> createTopic(String topic, List<MultipartFile> files, int sessionSize, String username) throws Exception {
        return createTopicCommand.execute(topic, files, sessionSize, username);
    }

    @Override
    public StateResponse<Object> updateTopic(String id, String newTopicName, String username) {
        return updateTopicCommand.execute(id, newTopicName, username);
    }

    @Override
    public StateResponse<Object> deleteTopic(String id, String username) {
        return deleteTopicCommand.execute(id, username);
    }

    @Override
    public StateResponse<Object> submitQuiz(QuizSubmissionRequest request, String username) {
        return evaluationFacade.submitQuiz(request, username);
    }

    @Override
    public StateResponse<Object> getUserStats(String username, String topic) {
        return analyticsFacade.getUserStats(username, topic);
    }

    @Override
    public StateResponse<Object> getOverviewStats(String username) {
        return analyticsFacade.getOverviewStats(username);
    }

    @Override
    public StateResponse<Object> addFileToTopic(MultipartFile file, String id, String username) throws Exception {
        return addFileToTopicCommand.execute(file, id, username);
    }

    @Override
    public StateResponse<Object> getTopicFiles(String id, String username) {
        return getTopicFilesQuery.execute(id, username);
    }

    @Override
    public StateResponse<Object> getAllTopicsInfo(String username) {
        return getTopicInfoQuery.execute(username);
    }

    @Override
    public StateResponse<Object> getTopicScoreHistory(String id, String username) {
        return getTopicScoreHistoryQuery.execute(id, username);
    }
}
