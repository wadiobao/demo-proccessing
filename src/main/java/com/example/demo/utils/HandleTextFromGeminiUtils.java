package com.example.demo.utils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.example.demo.modules.quiz.shared.domain.model.Answer;
import com.example.demo.modules.quiz.shared.domain.model.Question;
import com.example.demo.modules.document.shared.domain.model.TopicAndTags;
import com.google.genai.types.GenerateContentResponse;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import lombok.extern.slf4j.Slf4j;

/**
 * Specialized parser for AI-generated response content.
 * 
 * <p>
 * Chứa các logic phức tạp để bóc tách dữ liệu từ phản hồi của Gemini AI,
 * bao gồm việc làm sạch JSON, sửa lỗi cú pháp tự động và Regex cho định dạng
 * văn bản thô.
 *
 * @since 1.0
 */
@Component
@Slf4j
public class HandleTextFromGeminiUtils {

    public List<Question> parseQuestions(String inputText) {
        List<Question> questions = new ArrayList<>();
        // Regex để tìm từng khối câu hỏi
        // Pattern này tìm:
        // {Câu hỏi (\d+): (.*?) A\. (.*?) B\. (.*?) C\. (.*?) D\. (.*?) Đáp án đúng:
        // ([A-D])}
        // Group 1: ID câu hỏi (số)
        // Group 2: Nội dung câu hỏi (trước A.)
        // Group 3: Đáp án A (giữa A. và B.)
        // Group 4: Đáp án B (giữa B. và C.)
        // Group 5: Đáp án C (giữa C. và D.)
        // Group 6: Đáp án D (giữa D. và Đáp án đúng:)
        // Group 7: Đáp án đúng (A, B, C, hoặc D)
        Pattern pattern = Pattern.compile(
                "\\{?Câu hỏi\\s*(?:\\[?(\\d+)\\]?):\\s*(.*?)\\s*A\\.\\s*(.*?)\\s*B\\.\\s*(.*?)\\s*C\\.\\s*(.*?)\\s*D\\.\\s*(.*?)\\s*Đáp án đúng:\\s*([A-D])\\s*Giải thích:\\s*(.*?)(?=\\s*\\{?Câu hỏi|$)\\}?",
                Pattern.DOTALL);

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

                Answer answer = Answer.builder()
                		.option1(ansA)
                		.option2(ansB)
                		.option3(ansC).option4(ansD)
                		.correctAnswer(correctAns)
                		.explanation(explain)
                        .build();
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

    /**
     * Parses a list of questions from highly variable JSON or raw text formats.
     * 
     * <p>
     * Sử dụng cơ chế "Salvage" để cố gắng khôi phục dữ liệu ngay cả khi
     * phản hồi JSON bị cắt cụt hoặc chứa lỗi cú pháp từ AI.
     *
     * @param inputText raw AI output / kết quả thô từ AI
     * @return list of validated Questions / danh sách câu hỏi hợp lệ
     */
    public List<Question> parseQuestionsV4(String inputText) {
        if (inputText == null || inputText.trim().isEmpty()) {
            return new ArrayList<>();
        }

        Gson gson = new com.google.gson.GsonBuilder().setStrictness(com.google.gson.Strictness.LENIENT).create();
        Type questionListType = new TypeToken<ArrayList<Question>>() {
        }.getType();

        try {
            // First, try parsing the entire input as a JsonElement
            String cleanText = inputText.trim();
            // strip markdown artifacts to expose pure JSON payload
            // / loại bỏ các thành phần markdown để truy xuất nội dung JSON thuần túy
            if (cleanText.startsWith("```json")) {
                cleanText = cleanText.substring(7);
            }
            if (cleanText.startsWith("```")) {
                cleanText = cleanText.substring(3);
            }
            if (cleanText.endsWith("```")) {
                cleanText = cleanText.substring(0, cleanText.length() - 3);
            }
            cleanText = cleanText.trim();

            JsonElement rootElement = JsonParser.parseString(cleanText);
            JsonArray targetArray = null;

            if (rootElement.isJsonArray()) {
                targetArray = rootElement.getAsJsonArray();
            } else if (rootElement.isJsonObject()) {
                // If it's an object, look for a standard key like "questions" or just take the
                // first array we find
                JsonObject obj = rootElement.getAsJsonObject();
                if (obj.has("questions") && obj.get("questions").isJsonArray()) {
                    targetArray = obj.getAsJsonArray("questions");
                } else if (obj.has("data") && obj.get("data").isJsonArray()) {
                    targetArray = obj.getAsJsonArray("data");
                } else {
                    // Fallback: finding the first JsonArray value in the object
                    for (java.util.Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                        if (entry.getValue().isJsonArray()) {
                            targetArray = entry.getValue().getAsJsonArray();
                            break;
                        }
                    }
                }
            }

            if (targetArray != null) {
                return gson.fromJson(targetArray, questionListType);
            } else {
                System.err.println("Could not find a JSON array in the response.");
                // Last resort: try the old naive string extraction just in case
                String fallbackStr = extractJsonArrayString(inputText);
                if (fallbackStr != null) {
                    return gson.fromJson(fallbackStr, questionListType);
                }
                return new ArrayList<>();
            }

        } catch (com.google.gson.JsonSyntaxException e) {
            System.err.println("JSON Parse Error (possibly truncated), attempting to salvage: " + e.getMessage());
            // salvage mode: attempt recovery from truncated or syntactically invalid AI
            // output
            // / chế độ cứu hộ: cố gắng khôi phục dữ liệu từ kết quả AI bị cắt cụt hoặc sai
            // cú pháp
            String clean = extractJsonArrayString(inputText);
            if (clean == null) {
				return new ArrayList<>();
			}

            int lastBrace = clean.lastIndexOf('}');
            if (lastBrace > 0) {
                int secondLastBrace = clean.lastIndexOf('}', lastBrace - 1);
                if (secondLastBrace > 0) {
                    String salvaged = clean.substring(0, secondLastBrace + 1) + "]";
                    try {
                        List<Question> result = gson.fromJson(salvaged, questionListType);
                        log.info("Successfully salvaged {} questions.", result.size());
                        return result;
                    } catch (Exception ex) {
                        System.err.println("Salvage failed: " + ex.getMessage());
                    }
                } else {
                    String salvaged = clean.substring(0, lastBrace + 1) + "]";
                    try {
                        return gson.fromJson(salvaged, questionListType);
                    } catch (Exception ex) {
                    }
                }
            }
            throw e; // rethrow if we couldn't salvage
        }
    }

    /**
     * Extracts topic and metadata tags from AI response.
     * 
     * @param inputText raw AI output / kết quả thô từ AI
     * @return DTO containing topic and tag list / đối tượng chứa chủ đề và danh
     *         sách thẻ
     */
    @Deprecated(since = "1.0", forRemoval = true)
    public TopicAndTags parseTopicAndTags(String inputText) {
        if (inputText == null || inputText.trim().isEmpty()) {
            return new TopicAndTags();
        }

        Gson gson = new com.google.gson.GsonBuilder().setStrictness(com.google.gson.Strictness.LENIENT).create();
        Type type = new TypeToken<TopicAndTags>() {
        }.getType();

        try {
            String cleanText = inputText.trim();
            if (cleanText.startsWith("```json")) {
                cleanText = cleanText.substring(7);
            }
            if (cleanText.startsWith("```")) {
                cleanText = cleanText.substring(3);
            }
            if (cleanText.endsWith("```")) {
                cleanText = cleanText.substring(0, cleanText.length() - 3);
            }
            cleanText = cleanText.trim();

            JsonElement rootElement = JsonParser.parseString(cleanText);

            if (rootElement.isJsonObject()) {
                return gson.fromJson(rootElement, type);
            } else if (rootElement.isJsonArray() && !rootElement.getAsJsonArray().isEmpty()) {
                return gson.fromJson(rootElement.getAsJsonArray().get(0), type);
            }
        } catch (com.google.gson.JsonSyntaxException e) {
            System.err.println("JSON Parse Error for TopicAndTags, attempting fallback: " + e.getMessage());
        }

        String clean = extractJsonObject(inputText);
        if (clean != null) {
            try {
                return gson.fromJson(clean, type);
            } catch (Exception ex) {
                System.err.println("Fallback failed: " + ex.getMessage());
            }
        }

        return new TopicAndTags();
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
