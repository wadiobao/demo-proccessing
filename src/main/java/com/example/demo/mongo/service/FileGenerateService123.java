package com.example.demo.mongo.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dto.StateResponse;
import com.example.demo.mongo.dto.question.FileGenerateResponse;
import com.example.demo.mongo.dto.question.Question;
import com.example.demo.mongo.dto.question.UserQuestion;
import com.example.demo.utils.FileGeneratorUtils;


@Service
public class FileGenerateService123 {
	
	@Autowired
	private FileGeneratorUtils fileGeneratorUtils;
	
	@Autowired
	private UserQuestion userQuestion;
	
	public StateResponse<Object> generateWordAndPdfBase64(){
//		Map<String,Object> data = Map.ofEntries(
//				Map.entry("question", "day la cau hoi"),
//				Map.entry("answer", "day la cau tra loi")
//				);
		
		List<Map<String, Object>> formattedQuestions = new ArrayList<>();

        // Kiểm tra null an toàn
        if (userQuestion == null || userQuestion.getQuestionList() == null) {
            
        }

        // Lặp qua từng Question trong danh sách
        for (Question q : userQuestion.getQuestionList()) {
            // Kiểm tra null cho từng Question và Answer của nó
            if (q != null && q.getAnswer() != null) {
                // Sử dụng HashMap để linh hoạt hơn, đặc biệt nếu giá trị có thể null
                // Nếu bạn chắc chắn không có null và muốn map bất biến, có thể dùng Map.of
                Map<String, Object> questionMap = new HashMap<>();

                // Lấy dữ liệu từ Question và Answer, kiểm tra null và gán giá trị mặc định ""
                questionMap.put("id", q.getId() != 0 ? q.getId() : "");
                questionMap.put("question", q.getQuestion() != null ? q.getQuestion() : "");
                questionMap.put("optionA", q.getAnswer().getA() != null ? q.getAnswer().getA() : "");
                questionMap.put("optionB", q.getAnswer().getB() != null ? q.getAnswer().getB() : "");
                questionMap.put("optionC", q.getAnswer().getC() != null ? q.getAnswer().getC() : "");
                questionMap.put("optionD", q.getAnswer().getD() != null ? q.getAnswer().getD() : "");
                questionMap.put("answer", q.getAnswer().getCorrect() != null ? q.getAnswer().getCorrect() : "");

                formattedQuestions.add(questionMap);
            } else {
                // Tùy chọn: Ghi log cảnh báo nếu có Question hoặc Answer bị null
                System.err.println("Cảnh báo: Bỏ qua Question hoặc Answer bị null.");
            }
        }

		Map<String, Object> data = Map.of("questions", formattedQuestions);
		try {
			String word =  fileGeneratorUtils.generateWordFromTemplateReturnBase64("/templates/docx_template.docx", data);
			String pdf = fileGeneratorUtils.exportPdfBase64("/templates/docx_template.docx", data);
			return StateResponse.builder().result(FileGenerateResponse.builder().wordBase64(word).pdfBase64(pdf).build()).build();
		}catch (Exception e) {
			e.getMessage();
		}
		return null;
	}
}
