package com.example.demo.utils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.example.demo.mongo.dto.question.Answer;
import com.example.demo.mongo.dto.question.Question;
import com.example.demo.mongo.dto.TopicAndTags;
import com.google.genai.types.GenerateContentResponse;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

@Component
public class HandleTextFromGeminiUtils {
	
	public List<Question> parseQuestions(String inputText) {
        List<Question> questions = new ArrayList<>();
        // Regex để tìm từng khối câu hỏi
        // Pattern này tìm:
        // {Câu hỏi (\d+): (.*?) A\. (.*?) B\. (.*?) C\. (.*?) D\. (.*?) Đáp án đúng: ([A-D])}
        // Group 1: ID câu hỏi (số)
        // Group 2: Nội dung câu hỏi (trước A.)
        // Group 3: Đáp án A (giữa A. và B.)
        // Group 4: Đáp án B (giữa B. và C.)
        // Group 5: Đáp án C (giữa C. và D.)
        // Group 6: Đáp án D (giữa D. và Đáp án đúng:)
        // Group 7: Đáp án đúng (A, B, C, hoặc D)
        Pattern pattern = Pattern.compile(
        	    "\\{?Câu hỏi\\s*(?:\\[?(\\d+)\\]?):\\s*(.*?)\\s*A\\.\\s*(.*?)\\s*B\\.\\s*(.*?)\\s*C\\.\\s*(.*?)\\s*D\\.\\s*(.*?)\\s*Đáp án đúng:\\s*([A-D])\\s*Giải thích:\\s*(.*?)(?=\\s*\\{?Câu hỏi|$)\\}?",
        	    Pattern.DOTALL
        	);


        
        Matcher matcher = pattern.matcher(inputText);

        while (matcher.find()) {
            try {
                int id = Integer.parseInt(matcher.group(1).trim());
                String questionText = matcher.group(2).trim();
                String ansA = matcher.group(3).trim();
                String ansB = matcher.group(4).trim();
                String ansC = matcher.group(5).trim();
                String ansD = matcher.group(6).trim();
                String correctAns = matcher.group(7).trim();
                String explain = matcher.group(8).trim();

                Answer answer = Answer.builder().A(ansA).B(ansB).C(ansC).D(ansD).correct(correctAns).explain(explain).build();
                Question question = Question.builder().id(id).question(questionText).answer(answer).build();
                questions.add(question);
            } catch (NumberFormatException e) {
                System.err.println("Lỗi khi parse ID câu hỏi: " + matcher.group(1));
            } catch (Exception e) {
                System.err.println("Lỗi khi parse khối: " + matcher.group(0));
                e.printStackTrace();
            }
        }

        return questions;
    }
	
	public List<Question> parseQuestionsV4(String inputText) {
		String clean = extractJsonArrayString(inputText);
		//System.out.println(clean);
		Gson gson = new Gson();
		Type questionListType = new TypeToken<ArrayList<Question>>(){}.getType();
		List<Question> questions = gson.fromJson(clean, questionListType);
		//System.out.println(questions.toString());
        return questions;
    }
	
	public TopicAndTags parseTopicAndTags(String inputText) {
		System.out.println(inputText);
		String clean = extractJsonObject(inputText);
		System.out.println(clean);
		Gson gson = new Gson();
		Type type = new TypeToken<TopicAndTags>(){}.getType();
		TopicAndTags topicAndTags = gson.fromJson(clean, type);
		System.out.println(topicAndTags.toString());
        return topicAndTags;
    }
	
	public static String extractJsonArrayString(String rawInput) {
        int startIndex = rawInput.indexOf('[');
        int endIndex = rawInput.lastIndexOf(']');

        // Kiểm tra xem cả hai dấu ngoặc có tồn tại không và đúng thứ tự
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            return rawInput.substring(startIndex, endIndex + 1);
        }

        return null; // Không tìm thấy JSON array hợp lệ
    }
	
	public static String extractJsonObject(String input) {
	    if (input == null) {
			return null;
		}

	    int start = input.indexOf('{');
	    int end = input.lastIndexOf('}');

	    if (start == -1 || end == -1 || start > end) {
	        throw new IllegalArgumentException("Không tìm thấy JSON hợp lệ");
	    }

	    return input.substring(start, end + 1);
	}

	
	
	public static String extractDataFromGemini(GenerateContentResponse response) {
		String base64Data = null;

        try {
            // Bước 1: Phân tích chuỗi JSON thành một cây đối tượng
            JsonObject rootObject = JsonParser.parseString(response.toJson()).getAsJsonObject();

            // Bước 2: Đi sâu vào cấu trúc để lấy mảng "parts"
            JsonArray candidates = rootObject.getAsJsonArray("candidates");
            if (candidates != null && candidates.size() > 0) {
                JsonObject content = candidates.get(0).getAsJsonObject().getAsJsonObject("content");
                JsonArray parts = content.getAsJsonArray("parts");

                // Bước 3: Lặp qua mảng "parts" để tìm phần tử chứa "inlineData"
                for (JsonElement partElement : parts) {
                    JsonObject partObject = partElement.getAsJsonObject();
                    
                    // KIỂM TRA xem phần tử này có chứa "inlineData" không
                    if (partObject.has("inlineData")) {
                        // Nếu có, đi vào và lấy chuỗi "data"
                        JsonObject inlineDataObject = partObject.getAsJsonObject("inlineData");
                        base64Data = inlineDataObject.get("data").getAsString();
                        
                        // Đã tìm thấy, thoát khỏi vòng lặp
                        break; 
                    }
                }
            }
            
            return base64Data;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    
    }
	
	
	
}
