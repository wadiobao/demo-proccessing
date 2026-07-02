package com.example.demo.modules.quiz.generation.api.internal;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.quiz.generation.api.GenerationFacade;
import com.example.demo.modules.quiz.generation.application.usecase.GeneratePersonalizedQuizUseCase;
import com.example.demo.modules.quiz.generation.application.usecase.GenerateStandardQuizUseCase;
import com.example.demo.modules.quiz.generation.application.usecase.PersistQuizUseCase;
import com.example.demo.modules.quiz.shared.domain.model.FileGenerateResponse;
import com.example.demo.modules.quiz.shared.domain.model.QuizConfig;

import lombok.RequiredArgsConstructor;

/**
 * Implementation of GenerationFacade.
 */
@Service
@RequiredArgsConstructor
class GenerationFacadeImpl implements GenerationFacade {

    private final GenerateStandardQuizUseCase generateStandardQuizUseCase;
    private final GeneratePersonalizedQuizUseCase generatePersonalizedQuizUseCase;
    private final PersistQuizUseCase persistQuizUseCase;

    @Override
    public StateResponse<Object> generateStandardQuiz(MultipartFile file, QuizConfig config, String contentId) {
        return generateStandardQuizUseCase.execute(file, config, contentId);
    }

    @Override
    public StateResponse<Object> generateStandardQuiz(String text, String fileName, QuizConfig config, String contentId) {
        return generateStandardQuizUseCase.execute(text, fileName, config, contentId);
    }

    @Override
    public StateResponse<Object> generatePersonalizedQuiz(List<String> chunks, QuizConfig config, String requestId) {
        return generatePersonalizedQuizUseCase.execute(chunks, config, requestId);
    }


    @Override
    public FileGenerateResponse persistQuiz(FileGenerateResponse response, String username, String fileName, String rawText) throws Exception {
        return persistQuizUseCase.execute(response, username, fileName, rawText, true);
    }

    @Override
    public FileGenerateResponse persistQuiz(FileGenerateResponse response, String username, String fileName, String rawText, boolean shouldUpdateContentIds) throws Exception {
        return persistQuizUseCase.execute(response, username, fileName, rawText, shouldUpdateContentIds, null, "ADAPTIVE");
    }

    @Override
    public FileGenerateResponse persistQuiz(FileGenerateResponse response, String username, String fileName, String rawText, boolean shouldUpdateContentIds, String explicitTopic) throws Exception {
        return persistQuizUseCase.execute(response, username, fileName, rawText, shouldUpdateContentIds, explicitTopic, "ADAPTIVE");
    }

    @Override
    public FileGenerateResponse persistQuiz(FileGenerateResponse response, String username, String fileName, String rawText, boolean shouldUpdateContentIds, String explicitTopic, String type) throws Exception {
        return persistQuizUseCase.execute(response, username, fileName, rawText, shouldUpdateContentIds, explicitTopic, type);
    }
}
