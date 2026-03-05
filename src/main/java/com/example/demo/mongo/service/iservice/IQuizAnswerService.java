package com.example.demo.mongo.service.iservice;

import com.example.demo.dto.StateResponse;
import com.example.demo.mongo.dto.quiz.QuizSubmissionRequest;

/**
 * Interface contract for quiz submission and evaluation services.
 * 
 * <p>
 * Định nghĩa các tiêu chuẩn cho việc chấm điểm, cập nhật tham số IRT
 * và truy xuất số liệu thống kê học tập của người dùng.
 *
 * @since 1.0
 */
public interface IQuizAnswerService {

    /**
     * Processes user's quiz answers and updates their learning profile.
     *
     * @param request  Quiz submission with answers
     * @param username Authenticated user's username
     * @return StateResponse containing submission results and updated IRT
     *         parameters
     * @throws Exception if submission is invalid or duplicate
     */
    StateResponse<Object> submitQuizAnswers(QuizSubmissionRequest request, String username) throws Exception;

}
