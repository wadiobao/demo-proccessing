package com.example.demo.modules.quiz.generation.application.usecase;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.document.metadata.api.DocumentMetadataFacade;
import com.example.demo.modules.document.metadata.domain.model.DocumentMetadata;
import com.example.demo.modules.document.processing.api.DocumentProcessingFacade;
import com.example.demo.modules.document.retrieval.api.DocumentRetrievalFacade;
import com.example.demo.modules.quiz.generation.application.port.QuestionBankPort;
import com.example.demo.modules.quiz.generation.application.service.AdvancedQuizContextService;
import com.example.demo.modules.quiz.shared.application.QuizPromptBuilder;
import com.example.demo.modules.quiz.shared.application.QuizResponseBuilder;
import com.example.demo.modules.quiz.shared.domain.model.FileGenerateResponse;
import com.example.demo.modules.quiz.shared.domain.model.Question;
import com.example.demo.modules.quiz.shared.domain.model.QuizConfig;
import com.example.demo.modules.quiz.shared.domain.port.AiGenerationPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * UseCase for Standard Quiz Generation (Level 0, 1).
 * strictly supports single file input.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GenerateStandardQuizUseCase {

    private final DocumentProcessingFacade documentProcessingFacade;
    private final DocumentRetrievalFacade documentRetrievalFacade;
    private final DocumentMetadataFacade documentMetadataFacade;
    private final AiGenerationPort aiGenerationPort;
    private final QuestionBankPort questionBankPort;
    private final QuizPromptBuilder promptBuilder;
    private final QuizResponseBuilder responseBuilder;
    private final AdvancedQuizContextService advancedQuizContextService;

    public StateResponse<Object> execute(MultipartFile file, QuizConfig config, String contentId) {
        try {
            String pdfText = documentProcessingFacade.processDocument(file).getRawText();
            return execute(pdfText, file.getOriginalFilename(), config, contentId);
        } catch (Exception e) {
            log.error("Standard quiz generation failed: {}", e.getMessage(), e);
            return responseBuilder.buildFileGenerationError();
        }
    }

    public StateResponse<Object> execute(String pdfText, String fileName, QuizConfig config, String contentId) {
        try {
            log.info("Generating standard quiz from text: {} (Level: {})", fileName, config.getLevel());

            if (config.getLevel() >= 2) {
                throw new RuntimeException("Standard UseCase only supports Level 0 and 1.");
            }

            List<Question> questions = generateQuestions(config, pdfText, contentId);

            String[] wordAndPdf = documentRetrievalFacade.generateQuizDocuments(questions);
            if (wordAndPdf == null) {
                return responseBuilder.buildFileGenerationError();
            }

            FileGenerateResponse response = FileGenerateResponse.builder()
                    .questions(questions)
                    .wordBase64(wordAndPdf[0])
                    .pdfBase64(wordAndPdf[1])
                    .contentPdf(pdfText)
                    .topic(config.getTopic())
                    .build();

            return responseBuilder.buildSuccessResponse(response);

        } catch (Exception e) {
            log.error("Standard quiz generation from text failed: {}", e.getMessage(), e);
            return responseBuilder.buildFileGenerationError();
        }
    }

    private List<Question> generateQuestions(QuizConfig config, String pdfText, String contentId) {
        String relatedContext = null;
        if (config.getLevel() == 1) {
            List<String> sourceTags = getSourceTags(contentId, pdfText);
            AdvancedQuizContextService.CrossContextResult crossResult = 
                advancedQuizContextService.retrieveRelatedContext(contentId != null ? contentId : "temp_id", sourceTags, config.getTopic());
            relatedContext = crossResult.getSnippetB();
        }

        if (contentId != null && questionBankPort.countByContentId(contentId) >= 100) {
            return generateHybrid(config, pdfText, contentId, relatedContext);
        }

        QuizPromptBuilder.PromptContext context = promptBuilder.buildStandardPrompt(config, pdfText, relatedContext);
        log.info("[AI-CALL] Generating standard AI questions (Instruction length: {})", context.instruction().length());
        List<Question> questions = aiGenerationPort.generateQuestions(context.instruction(), context.userMessage()).questions();
        log.info("[AI-RESULT] Generated {} standard questions", questions.size());
        return questions;
    }

    private List<Question> generateHybrid(QuizConfig config, String pdfText, String contentId, String relatedContext) {
        int totalNeeded = config.getQuestionCount();
        int fromBankCount = totalNeeded / 2;
        int fromAiCount = totalNeeded - fromBankCount;

        List<Question> questions = new ArrayList<>(questionBankPort.getRandomQuestions(contentId, fromBankCount));
        config.setQuestionCount(fromAiCount);
        
        QuizPromptBuilder.PromptContext context = promptBuilder.buildStandardPrompt(config, pdfText, relatedContext);
        List<Question> aiQuestions = aiGenerationPort.generateQuestions(context.instruction(), context.userMessage()).questions();
        questions.addAll(aiQuestions);
        
        config.setQuestionCount(totalNeeded);
        for (int i = 0; i < questions.size(); i++) {
			questions.get(i).setId(i + 1);
		}
        return questions;
    }

    private List<String> getSourceTags(String contentId, String pdfText) {
        if (contentId != null) {
            DocumentMetadata metadata = documentMetadataFacade.findById(contentId);
            if (metadata != null && metadata.getTags() != null) {
				return metadata.getTags();
			}
        }
        return documentProcessingFacade.analyzeText(pdfText).getKeywords();
    }
}
