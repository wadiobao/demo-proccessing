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

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;

import com.example.demo.mongo.entity.QuestionBank;
import com.example.demo.mongo.service.quiz.WordPdfGeneration;

/**
 * Engine for automated quiz construction and format processing.
 * 
 * <p>
 * Chịu trách nhiệm điều phối việc trích xuất văn bản, gọi AI tạo câu hỏi
 * theo mô hình kết hợp (Hybrid) và tạo các tệp tài liệu đi kèm (Word/PDF).
 *
 * @since 1.0
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
    MongoTemplate mongoTemplate;

    /**
     * Processes a PDF file and generates a quiz.
     */
    public StateResponse<Object> processQuiz(MultipartFile file, QuizConfig config) {
        return processQuiz(file, config, null);
    }

    /**
     * Standard entry point for quiz generation from raw file and config.
     * 
     * <p>
     * Xử lý đồng bộ từ khâu tiền xử lý file đến khi ra kết quả câu hỏi cuối cùng.
     * 
     * @param file      source document / tài liệu nguồn
     * @param config    user preferences / cấu hình mong muốn
     * @param contentId optional existing reference / mã định danh nội dung (nếu có)
     * @return standardized state response / phản hồi trạng thái chuẩn
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

            // TODO: [ROADMAP] Synchronous image generation is disabled to prevent request
            // timeouts.
            // Future: implement async image generation via task queue (e.g., Spring @Async
            // / event-driven).
            // When re-enabling, replace code below:
            // List<Question> questions = geminiResponse.getQuestions();
            // if (config.getImgQuest() == 1) { generateQuestionImages(questions); }
            List<Question> questions = geminiResponse.getQuestions();

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

                // 1. Get questions from Bank (Randomly sampled via MongoDB aggregate)
                // / Lấy câu hỏi từ Ngân hàng (Lấy mẫu ngẫu nhiên qua MongoDB aggregate để tối
                // ưu bộ nhớ)
                Aggregation aggregation = Aggregation.newAggregation(
                        Aggregation.match(Criteria.where("contentId").is(contentId)),
                        Aggregation.sample(fromBank));
                List<QuestionBank> bankedItems = mongoTemplate
                        .aggregate(aggregation, QuestionBank.class, QuestionBank.class).getMappedResults();

                List<Question> selectedFromBank = bankedItems.stream()
                        .map(QuestionBank::getQuestionData)
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
