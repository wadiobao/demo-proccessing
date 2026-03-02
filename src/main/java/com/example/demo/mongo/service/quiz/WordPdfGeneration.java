package com.example.demo.mongo.service.quiz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.constants.Constants;
import com.example.demo.mongo.dto.question.Question;
import com.example.demo.utils.FileGeneratorUtils;

/**
 * Utility for converting generated quiz data into downloadable document
 * formats.
 * 
 * <p>
 * Chuyển đổi danh sách câu hỏi AI thành các tệp Microsoft Word và PDF
 * dựa trên các mẫu (Template) có sẵn để người dùng tải về.
 *
 * @since 1.0
 */
@Component
public class WordPdfGeneration {

    @Autowired
    private FileGeneratorUtils fileGeneratorUtils;

    /**
     * Generates both Word and PDF versions of the quiz in Base64 format.
     * 
     * @param questions list of quiz questions / danh sách câu hỏi bài tập
     * @return array [wordBase64, pdfBase64] or null on failure / mảng chuỗi Base64
     *         hoặc null nếu lỗi
     */
    public String[] generateWordAndPdfBase64(List<Question> questions) {
        if (questions == null || questions.isEmpty()) {
            return null;
        }

        List<Map<String, Object>> formattedQuestions = new ArrayList<>();

        for (Question q : questions) {
            if (q != null && q.getAnswer() != null) {
                Map<String, Object> questionMap = new HashMap<>();
                questionMap.put(Constants.MapKeys.ID, q.getId() != 0 ? q.getId() : "");
                questionMap.put(Constants.MapKeys.QUESTION, q.getQuestion() != null ? q.getQuestion() : "");
                questionMap.put(Constants.MapKeys.OPTION_A, q.getAnswer().getA() != null ? q.getAnswer().getA() : "");
                questionMap.put(Constants.MapKeys.OPTION_B, q.getAnswer().getB() != null ? q.getAnswer().getB() : "");
                questionMap.put(Constants.MapKeys.OPTION_C, q.getAnswer().getC() != null ? q.getAnswer().getC() : "");
                questionMap.put(Constants.MapKeys.OPTION_D, q.getAnswer().getD() != null ? q.getAnswer().getD() : "");
                questionMap.put(Constants.MapKeys.ANSWER,
                        q.getAnswer().getCorrect() != null ? q.getAnswer().getCorrect() : "");
                questionMap.put(Constants.MapKeys.EXPLAIN,
                        q.getAnswer().getExplain() != null ? q.getAnswer().getExplain() : "");
                formattedQuestions.add(questionMap);
            } else {
                System.err.println("Cảnh báo: Bỏ qua Question hoặc Answer bị null.");
            }
        }

        Map<String, Object> data = Map.of(Constants.MapKeys.QUESTIONS, formattedQuestions);
        try {
            String word = fileGeneratorUtils.generateWordFromTemplateReturnBase64(Constants.FilePaths.TEST_TEMPLATE,
                    data);
            String pdf = fileGeneratorUtils.exportPdfBase64(Constants.FilePaths.TEST_TEMPLATE, data);
            return new String[] { word, pdf };
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
