package com.example.demo.modules.document.retrieval.application.usecase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.demo.constants.Constants;
import com.example.demo.mongo.dto.question.Question;
import com.example.demo.modules.document.retrieval.application.port.output.DocumentGeneratorPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Use case for generating quiz documents (Word/PDF).
 * 
 * <p>
 * Chuyển đổi dữ liệu câu hỏi thành cấu trúc Map phù hợp với Template
 * và phối hợp với Port để tạo file Base64.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GenerateQuizDocumentUseCase {

    private final DocumentGeneratorPort documentGeneratorPort;

    /**
     * Generates Word and PDF versions of the given questions.
     * 
     * @param questions list of questions
     * @return array [wordBase64, pdfBase64]
     */
    public String[] execute(List<Question> questions) {
        if (questions == null || questions.isEmpty()) {
            return null;
        }

        Map<String, Object> data = Map.of(Constants.MapKeys.QUESTIONS, formatQuestions(questions));
        
        try {
            String word = documentGeneratorPort.generateBase64(Constants.FilePaths.TEST_TEMPLATE, data, "DOCX");
            String pdf = documentGeneratorPort.generateBase64(Constants.FilePaths.TEST_TEMPLATE, data, "PDF");
            return new String[] { word, pdf };
        } catch (Exception e) {
            log.error("Failed to generate quiz documents: {}", e.getMessage());
            return null;
        }
    }

    private List<Map<String, Object>> formatQuestions(List<Question> questions) {
        List<Map<String, Object>> formatted = new ArrayList<>();
        for (Question q : questions) {
            if (q == null || q.getAnswer() == null) continue;
            
            Map<String, Object> map = new HashMap<>();
            map.put(Constants.MapKeys.ID, q.getId() != 0 ? q.getId() : "");
            map.put(Constants.MapKeys.QUESTION, q.getQuestion() != null ? q.getQuestion() : "");
            map.put(Constants.MapKeys.OPTION_A, q.getAnswer().getA() != null ? q.getAnswer().getA() : "");
            map.put(Constants.MapKeys.OPTION_B, q.getAnswer().getB() != null ? q.getAnswer().getB() : "");
            map.put(Constants.MapKeys.OPTION_C, q.getAnswer().getC() != null ? q.getAnswer().getC() : "");
            map.put(Constants.MapKeys.OPTION_D, q.getAnswer().getD() != null ? q.getAnswer().getD() : "");
            map.put(Constants.MapKeys.ANSWER, q.getAnswer().getCorrect() != null ? q.getAnswer().getCorrect() : "");
            map.put(Constants.MapKeys.EXPLAIN, q.getAnswer().getExplain() != null ? q.getAnswer().getExplain() : "");
            formatted.add(map);
        }
        return formatted;
    }
}
