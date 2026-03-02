package com.example.demo.mongo.service.quiz;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.StateResponse;
import com.example.demo.mongo.dto.question.FileGenerateResponse;
import com.example.demo.mongo.dto.question.Question;
import com.example.demo.mongo.dto.quiz.QuizConfig;
import com.example.demo.mongo.repository.QuestionBankRepository;
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
    QuestionBankRepository questionBankRepository;

    /**
     * Processes a PDF file and generates a quiz.
     */
    public StateResponse<Object> processQuiz(MultipartFile file, QuizConfig config) {
        return processQuiz(file, config, null);
    }

    /**
     * Processes a PDF file and generates a quiz with optional contentId.
     */
    public StateResponse<Object> processQuiz(MultipartFile file, QuizConfig config, String contentId) {
        try {
            // Select processor and extract text
            IDocumentProcessor processor = documentProcessorFactory.getProcessor(file);
            String pdfText = processor.extractText(file);
            return processQuiz(file, pdfText, config, contentId);
        } catch (Exception e) {
            log.error("Error processing quiz: {}", e.getMessage(), e);
            return responseBuilder.buildFileGenerationError();
        }
    }

    /**
     * Processes pre-extracted text and generates a quiz.
     */
    public StateResponse<Object> processQuiz(MultipartFile file, String pdfText, QuizConfig config) {
        return processQuiz(file, pdfText, config, null);
    }

    /**
     * Processes pre-extracted text and generates a quiz with hybrid support.
     */
    public StateResponse<Object> processQuiz(MultipartFile file, String pdfText, QuizConfig config, String contentId) {
        try {
            log.info("Processing content for file: {} (ContentId: {})", file.getOriginalFilename(), contentId);

            // Generate questions using Hybrid AI/Bank approach
            GeminiResponse geminiResponse = generateQuestions(config, pdfText, contentId);

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
     * Generates questions using AI or Hybrid approach based on bank size.
     */
    private GeminiResponse generateQuestions(QuizConfig config, String pdfText, String contentId) throws Exception {
        if (config.getLevel() == 2) {
            // Adaptive/Regeneration mode stays 100% AI for now as it's targeted tweak
            String prompt = promptBuilder.buildRegenerationPrompt(config, pdfText);
            return geminiAIService.reGenerateQuestionWithGemini(prompt);
        }

        // Check Hybrid Threshold (Bank >= 100 questions for this content)
        if (contentId != null) {
            long bankSize = questionBankRepository.countByContentId(contentId);
            if (bankSize >= 100) {
                int totalNeeded = config.getQuestionCount();
                int fromBank = totalNeeded / 2;
                int fromAI = totalNeeded - fromBank;

                log.info("Hybrid Generation triggered for content {}: {} from Bank, {} from AI", contentId, fromBank,
                        fromAI);

                // 1. Get questions from Bank (Randomly sampled)
                List<com.example.demo.mongo.entity.QuestionBank> bankedItems = questionBankRepository
                        .findAllByContentId(contentId);
                java.util.Collections.shuffle(bankedItems);
                List<Question> selectedFromBank = bankedItems.stream()
                        .limit(fromBank)
                        .map(com.example.demo.mongo.entity.QuestionBank::getQuestionData)
                        .collect(java.util.stream.Collectors.toList());

                // 2. Get remaining from AI
                config.setQuestionCount(fromAI);
                String prompt = promptBuilder.buildStandardPrompt(config, pdfText);
                GeminiResponse aiResponse = geminiAIService.generateQuestionWithGemini(prompt);

                // Reset config for consistency
                config.setQuestionCount(totalNeeded);

                // 3. Merge and Re-index IDs to ensure sequential order
                List<Question> combined = new java.util.ArrayList<>(selectedFromBank);
                combined.addAll(aiResponse.getQuestions());
                for (int i = 0; i < combined.size(); i++) {
                    combined.get(i).setId(i + 1);
                }

                return new GeminiResponse("Hybrid success", combined);
            }
        }

        // Default: 100% AI generation
        String prompt = promptBuilder.buildStandardPrompt(config, pdfText);
        log.debug("Standard Prompt generated for {} questions", config.getQuestionCount());
        return geminiAIService.generateQuestionWithGemini(prompt);
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
