package com.example.demo.mongo.service.quiz;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.StateResponse;
import com.example.demo.mongo.dto.question.FileGenerateResponse;
import com.example.demo.mongo.dto.question.Question;
import com.example.demo.mongo.dto.quiz.QuizConfig;
import com.example.demo.mongo.service.quiz.GeminiAIUtils.GeminiResponse;
import com.example.demo.mongo.service.quiz.processor.DocumentProcessorContext;
import com.example.demo.mongo.service.quiz.processor.IDocumentProcessor;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 * Processes PDF files and generates quizzes using AI.
 * Handles both base (text-based) and scanned PDFs.
 * Follows Single Responsibility Principle.
 */
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class QuizProcessor {

    DocumentProcessorContext documentProcessorFactory;
    GeminiAIUtils geminiAIService;
    WordPdfGeneration fileGenerationService;
    QuizPromptBuilder promptBuilder;
    QuizResponseBuilder responseBuilder;

    /**
     * Processes a PDF file and generates a quiz.
     *
     * @param file   PDF file to process
     * @param config Quiz configuration
     * @return StateResponse containing the generated quiz or error
     */
    public StateResponse<Object> processQuiz(MultipartFile file, QuizConfig config) {
        try {
            // Select processor and extract text
            IDocumentProcessor processor = documentProcessorFactory.getProcessor(file);
            String pdfText = processor.extractText(file);
            return processQuiz(file, pdfText, config);
        } catch (Exception e) {
            log.error("Error processing quiz: {}", e.getMessage(), e);
            return responseBuilder.buildFileGenerationError();
        }
    }

    /**
     * Processes pre-extracted text and generates a quiz.
     */
    public StateResponse<Object> processQuiz(MultipartFile file, String pdfText, QuizConfig config) {
        try {
            log.info("Processing content for file: {} with {} characters", file.getOriginalFilename(),
                    pdfText.length());

            // Generate questions using AI
            GeminiResponse geminiResponse = generateQuestions(config, pdfText);

            // Generate downloadable files (Word & PDF)
            String[] wordAndPdf = fileGenerationService.generateWordAndPdfBase64(geminiResponse.getQuestions());
            if (wordAndPdf == null) {
                return responseBuilder.buildFileGenerationError();
            }

            // Process image generation if requested
            List<Question> questions = geminiResponse.getQuestions();
            if (config.getImgQuest() == 1) {
                generateQuestionImages(questions);
            }

            // Build final response
            FileGenerateResponse response = FileGenerateResponse.builder()
                    .questions(questions)
                    .wordBase64(wordAndPdf[0])
                    .pdfBase64(wordAndPdf[1])
                    .contentPdf(pdfText)
                    .build();

            return responseBuilder.buildSuccessResponse(response);

        } catch (Exception e) {
            log.error("Error processing quiz: {}", e.getMessage(), e);
            return responseBuilder.buildFileGenerationError();
        }
    }

    /**
     * Generates questions using AI based on configuration.
     */
    private GeminiResponse generateQuestions(QuizConfig config, String pdfText) throws Exception {
        String prompt;

        if (config.getLevel() == 2) {
            // Adaptive/Regeneration mode
            prompt = promptBuilder.buildRegenerationPrompt(config, pdfText);
            return geminiAIService.reGenerateQuestionWithGemini(prompt);
        } else {
            // Standard mode
            prompt = promptBuilder.buildStandardPrompt(config, pdfText);
            log.debug("Prompt preview: {}", prompt.substring(0, Math.min(70, prompt.length())));
            return geminiAIService.generateQuestionWithGemini(prompt);
        }
    }

    /**
     * Generates images for questions that have image prompts.
     */
    private void generateQuestionImages(List<Question> questions) {
        for (Question question : questions) {
            if (question.getImgPrompt() != null) {
                try {
                    String[] imgAttribute = geminiAIService.generateImageWithGemini(
                            question.getImgPrompt(),
                            question.getId());
                    question.setImgPublicId(imgAttribute[0]);
                    question.setImgUrl(imgAttribute[1]);
                } catch (Exception e) {
                    log.warn("Failed to generate image for question {}: {}", question.getId(), e.getMessage());
                }
            }
        }
    }
}
