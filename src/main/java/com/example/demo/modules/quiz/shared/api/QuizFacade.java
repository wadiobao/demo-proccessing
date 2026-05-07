package com.example.demo.modules.quiz.shared.api;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.quiz.adaptive.api.request.QuizSubmissionRequest;
import com.example.demo.modules.quiz.shared.domain.model.Question;
import com.example.demo.modules.quiz.shared.domain.model.QuizConfig;

/**
 * Public Facade for the Quiz & Learning module.
 * 
 * <p>
 * Điểm truy cập chính cho các Controller và Module khác để thực hiện 
 * các nghiệp vụ liên quan đến tạo bài tập, chấm điểm và quản lý ngân hàng câu hỏi.
 */
public interface QuizFacade {

    /**
     * Coordinate quiz generation from a file and configuration (Public Mode).
     */
    StateResponse<Object> generateQuiz(MultipartFile file, QuizConfig config);

    /**
     * Coordinate private quiz generation (authenticated users).
     * Includes adaptive difficulty adjustment and persistence.
     */
    StateResponse<Object> generatePrivateQuiz(List<MultipartFile> files, QuizConfig config, String username) throws Exception;

    /**
     * Submit quiz answers for evaluation and IRT update.
     */
    StateResponse<Object> submitQuiz(QuizSubmissionRequest request, String username);

    /**
     * Retrieve learning stats for a specific topic.
     */
    StateResponse<Object> getUserStats(String username, String topic);

    /**
     * Retrieve global overview of user learning progress.
     */
    StateResponse<Object> getOverviewStats(String username);

    /**
     * Commits a list of staged questions into the question bank.
     */
    void commitStagedQuestions(java.util.List<com.example.demo.modules.quiz.shared.domain.model.Question> questions, String username, String contentId);

    /**
     * Retrieves all questions associated with a specific content ID.
     */
    java.util.List<Question> getQuestionsByContentId(String contentId);
}
