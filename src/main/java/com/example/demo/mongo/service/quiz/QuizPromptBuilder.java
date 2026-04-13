package com.example.demo.mongo.service.quiz;

import java.util.Locale;

import org.springframework.stereotype.Component;

import com.example.demo.constants.Constants;
import com.example.demo.mongo.dto.quiz.QuizConfig;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Builder for constructing dynamic AI prompts for quiz generation.
 * 
 * <p>
 * Chuyển đổi các cấu hình người dùng và nội dung văn bản thành các
 * chỉ dẫn (Prompt) tối ưu cho mô hình ngôn ngữ lớn (LLM).
 *
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QuizPromptBuilder {

        /**
         * Builds a standard prompt for comprehensive quiz generation.
         *
         * @param config  generation settings / cấu hình tạo câu hỏi
         * @param pdfText source text / nội dung văn bản gốc
         * @return formatted prompt / chuỗi prompt hoàn chỉnh
         */
        public String buildStandardPrompt(QuizConfig config, String pdfText, String relatedContext) {
                String basePrompt = String.format(Locale.US,
                                Constants.QuestionFormat.QUESTION_COUNT
                                                + Constants.QuestionFormat.DIFFICULTY_LEVEL
                                                + Constants.QuestionFormat.KNOWLEDGE_TYPE
                                                + Constants.QuestionFormat.IMAGE_PRESENTATION
                                                + Constants.QuestionFormat.LANGUAGE
                                                + Constants.QuestionFormat.DOCUMENT_PROVIDED,
                                config.getQuestionCount(),
                                config.getLevel(),
                                config.getType(),
                                config.getImgQuest(),
                                config.getLanguage(),
                                pdfText);
                                
                if (relatedContext != null && !relatedContext.isEmpty() && !relatedContext.equals("NONE - FORCE_AI_EXTRAPOLATION")) {
                    basePrompt += String.format(Locale.US, Constants.QuestionFormat.CROSS_CONTEXT, relatedContext);
                } else if ("NONE - FORCE_AI_EXTRAPOLATION".equals(relatedContext)) {
                    basePrompt += "\n[CHÚ Ý FALLBACK]: Không có tài liệu chéo hệ thống cung cấp. Hãy TỰ SỬ DỤNG lượng kiến thức được huấn luyện bên ngoài để TỔNG HỢP KIẾN THỨC với tài liệu được cung cấp.";
                }
                
                return basePrompt;
        }

        /**
         * Builds a specialized prompt for adaptive difficulty adjustment (IRT).
         *
         * @param config  adaptive settings / cấu hình thích ứng
         * @param pdfText source text / nội dung văn bản gốc
         * @return targeted prompt / prompt tối ưu cho độ khó thích ứng
         */
        public String buildRegenerationPrompt(QuizConfig config, String pdfText) {
                return String.format(Locale.US,
                                Constants.QuestionFormat.QUESTION_COUNT
                                                + Constants.QuestionFormat.MIN_DIFFICULT
                                                + Constants.QuestionFormat.MAX_DIFFICULT
                                                + Constants.QuestionFormat.LANGUAGE
                                                + Constants.QuestionFormat.DOCUMENT_PROVIDED,
                                config.getQuestionCount(),
                                config.getMinDifficulty(),
                                config.getMaxDifficulty(),
                                config.getLanguage(),
                                pdfText);
        }

        /**
         * Builds a specialized prompt for identifying and extracting existing questions
         * from a document.
         *
         * @param pdfText source text / nội dung văn bản gốc
         * @return identification prompt / prompt trích xuất câu hỏi
         */
        public String buildIdentificationPrompt(String pdfText) {
                return String.format(Locale.US, Constants.QuestionFormat.DOCUMENT_PROVIDED, pdfText);
        }
}
