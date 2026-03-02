package com.example.demo.mongo.service.quiz;

import org.springframework.stereotype.Component;

import com.example.demo.constants.Constants;
import com.example.demo.dto.StateResponse;
import com.example.demo.mongo.dto.question.FileGenerateResponse;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Builder for constructing uniform API responses for quiz operations.
 * 
 * <p>
 * Chuẩn hóa dữ liệu đầu ra từ qúa trình tạo bài tập thành các
 * phản hồi trạng thái (StateResponse) nhất quán cho phía Frontend.
 *
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QuizResponseBuilder {

    /**
     * Builds a success response with quiz data.
     *
     * @param response Generated quiz data
     * @return StateResponse containing the quiz
     */
    public StateResponse<Object> buildSuccessResponse(FileGenerateResponse response) {
        return StateResponse.builder()
                .result(response)
                .build();
    }

    /**
     * Builds an error response with a message.
     *
     * @param message Error message
     * @return StateResponse with error message
     */
    public StateResponse<Object> buildErrorResponse(String message) {
        return StateResponse.builder()
                .message(message)
                .build();
    }

    /**
     * Builds a default file generation error response.
     *
     * @return StateResponse with standard error message
     */
    public StateResponse<Object> buildFileGenerationError() {
        return buildErrorResponse(Constants.Messages.FILE_GENERATION_ERROR);
    }
}
