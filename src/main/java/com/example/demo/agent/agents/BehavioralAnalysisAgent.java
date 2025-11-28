package com.example.demo.agent.agents;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.example.demo.agent.entity.Answer;
import com.example.demo.agent.entity.UserAnswer;
import com.example.demo.agent.entity.UserEvaluation;
import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import io.reactivex.rxjava3.core.Flowable;

public class BehavioralAnalysisAgent {

	private String USER_ID = null;
	private static String NAME = "AnalysisAgent";
	private static final String MODEL_NAME = "gemini-2.5-flash";
	private InMemoryRunner runner = null;
	private Session session = null;
	
	public static BaseAgent initAgent() {
		return LlmAgent.builder()
                .model(MODEL_NAME)
                .name(NAME)
                .description("Analysis agent")
                .instruction(
                        """
**Vai trò:** Bạn là một AI chuyên gia phân tích dữ liệu học tập, có khả năng biến dữ liệu thô về câu trả lời của người dùng thành một bản đánh giá năng lực chi tiết.

**Mục tiêu:** Nhận một đối tượng `UserAnswer` làm đầu vào. Dựa trên dữ liệu đó, hãy thực hiện các tính toán và suy luận cần thiết để tạo và trả về một đối tượng `UserEvaluation` đã được điền đầy đủ thông tin.

**Dữ liệu đầu vào ví dụ (Ví dụ JSON cho `UserAnswer`):**
```json
{
  "email": string,
  "sessionId": int,
  "score": double,
  "userResponses": [
    {
      "questionId": int,
      "question": string,
      "topic": string,
      "questionType": string,
      "userResponse": string,
      "correctAnswer": string,
      "isCorrect": bool,
      "timeSpendExpectedSeconds": int,
      "timeSpentSeconds": int,
      "questionLevel": int,
      "conceptTags": string[]
    },
   ]
}
```

**Nhiệm vụ chi tiết:** Hãy điền vào tất cả các trường của đối tượng `UserEvaluation` sau đây dựa trên các quy tắc bên dưới.

```java
public class UserEvaluation {
    String email;
	UserAnswer answer;
	String confidenceLevel;     // Suy luận: "Cao", "Trung bình", "Thấp"
	String learningLevel;       // Suy luận: "Nâng cao", "Trung bình", "Mới bắt đầu"
	List<String> conceptTags;  	// Suy luận: Các thẻ concept của người dùng
	double averageScore;        // Tính toán: Tỷ lệ đúng (%)
	double averageTimeSeconds;  // Tính toán: Thời gian trung bình
	double averageConfidence;   // Tính toán: Điểm tự tin trung bình (0.0 - 1.0)
}

**QUY TẮC THỰC HIỆN**

1.  **Trường dữ liệu gốc:**
    *   `answer`: Giữ nguyên toàn bộ đối tượng `UserAnswer` đầu vào.

2.  **Tính toán các chỉ số định lượng:**
    *   `averageScore`: Tính tỷ lệ phần trăm câu trả lời đúng. Công thức: `(số câu isCorrect: true / tổng số câu) * 100`.
    *   `averageTimeSeconds`: Tính thời gian làm bài trung bình của người dùng. Công thức: `tổng timeSpentSeconds / tổng số câu`.

3.  **Suy luận các thuộc tính định tính (CÓ CẬP NHẬT):**
    *   **`learningLevel` (Trình độ học tập):** Đánh giá dựa trên tỷ lệ làm đúng ở các `questionLevel`.
        *   **"Nâng cao"**: Nếu `averageScore` > 80% và người dùng thường xuyên trả lời đúng các câu `questionLevel` >= 3 **nhanh hơn hoặc bằng** `timeSpentExpectedSeconds`.
        *   **"Trung bình"**: Nếu `averageScore` trong khoảng 40%-80%, làm tốt các câu `questionLevel` 1-2 nhưng sai nhiều hoặc tốn nhiều thời gian hơn `timeSpentExpectedSeconds` ở các câu `questionLevel` >= 3.
        *   **"Mới bắt đầu"**: Nếu `averageScore` < 40% và sai nhiều ngay cả ở các câu `questionLevel` 1.
    *   **`confidenceLevel` (Mức độ tự tin tổng thể):** **Sử dụng `timeSpentExpectedSeconds` làm thước đo chính.**
        *   **"Cao"**: Người dùng trả lời đúng phần lớn câu hỏi VÀ `timeSpentSeconds` thường xuyên nhỏ hơn `timeSpentExpectedSeconds` (cho thấy sự thành thạo).
        *   **"Trung bình"**: Người dùng trả lời đúng nhưng `timeSpentSeconds` thường xấp xỉ hoặc lớn hơn `timeSpentExpectedSeconds` (cho thấy sự cẩn thận nhưng chưa thành thạo). Hoặc trả lời sai nhưng rất nhanh (đoán mò).
        *   **"Thấp"**: Người dùng trả lời sai và `timeSpentSeconds` lớn hơn `timeSpentExpectedSeconds` (cho thấy sự vật lộn, không nắm vững kiến thức).
    *   **`conceptTag` (Khái niệm cần cải thiện):**
        *   **Ưu tiên 1:** Tìm `conceptTag` xuất hiện nhiều nhất trong số các câu trả lời sai (`isCorrect: false`).
        *   **Ưu tiên 2:** Nếu có nhiều thẻ cùng số lần sai bằng nhau, hãy chọn thẻ có chênh lệch giữa `timeSpentSeconds` và `timeSpentExpectedSeconds` lớn nhất (người dùng mất nhiều thời gian hơn kỳ vọng nhất).

4.  **Tính toán chỉ số suy luận (CÓ CẬP NHẬT LỚN):**
    *   **`averageConfidence` (Độ tự tin trung bình):** Lượng hóa sự tự tin thành một con số từ 0.0 đến 1.0. Hãy tính điểm tự tin cho mỗi câu trả lời rồi lấy trung bình, **dựa trên so sánh giữa thời gian thực tế và thời gian kỳ vọng.**
        *   **Thành thạo (1.0):** Trả lời **Đúng** VÀ `timeSpentSeconds` <= `timeSpentExpectedSeconds`.
        *   **Cẩn thận (0.7):** Trả lời **Đúng** VÀ `timeSpentSeconds` > `timeSpentExpectedSeconds`.
        *   **Đoán mò/Vội vàng (0.3):** Trả lời **Sai** VÀ `timeSpentSeconds` < `timeSpentExpectedSeconds`.
        *   **Vật lộn/Không chắc (0.1):** Trả lời **Sai** VÀ `timeSpentSeconds` >= `timeSpentExpectedSeconds`.

**Định dạng đầu ra:**
Vui lòng trả về một đối tượng JSON duy nhất, có cấu trúc chính xác như lớp `UserEvaluation`.

*** """)
                //.tools(FunctionTool.create(ToolsForUserEvaluation.class,"saveUserEvaluation"))
                .outputKey("user_analysis_result")
                .build();
	}
	
	public void runAgent(String userID) {
		runner = new InMemoryRunner(initAgent());
		USER_ID = userID;
        session =
            runner
                .sessionService()
                .createSession(NAME, USER_ID)
                .blockingGet();
        
 	}
	
	public UserEvaluation analysis(String userAnswer) {
        Content userMsg = Content.fromParts(Part.fromText(userAnswer));
        Flowable<Event> events = runner.runAsync(USER_ID, session.id(), userMsg);
        StringBuilder raw = new StringBuilder();
        events.blockingForEach(event -> raw.append(event.stringifyContent())); //raw.append(event.stringifyContent()));
        String clean = raw.substring(7, raw.length()-3);        
        //System.out.println(clean);
        Gson gson = new Gson();
		Type type = new TypeToken<UserEvaluation>(){}.getType();
		UserEvaluation evaluation = gson.fromJson(clean, type);
        return evaluation;
	}
	
	public Map<String,String> calculateProperties(UserAnswer userAnswer){
		Map<String,String> properties = new HashMap<String, String>();
		Set<String> tags = new HashSet<String>();
		for (Answer answer : userAnswer.getUserResponses()) {
			tags.addAll(answer.getConceptTags());
			
		}
		return null;
	}
	
	public static void main(String[] args) {
		BehavioralAnalysisAgent agent = new BehavioralAnalysisAgent();
		agent.runAgent("demo");
		
        String userInput = "{\r\n"
        		+ "  \"email\": \"hieu.hoc.dia.ly@example.com\",\r\n"
        		+ "  \"sessionId\": 98765,\r\n"
        		+ "  \"score\": 60.0,\r\n"
        		+ "  \"userResponses\": [\r\n"
        		+ "    {\r\n"
        		+ "      \"questionId\": 101,\r\n"
        		+ "      \"question\": \"Sông nào dài nhất thế giới?\",\r\n"
        		+ "      \"topic\": \"Địa lý\",\r\n"
        		+ "      \"questionType\": \"SHORT_ANSWER\",\r\n"
        		+ "      \"answerOptions\": null,\r\n"
        		+ "      \"userResponse\": \"Sông Nile\",\r\n"
        		+ "      \"correctAnswer\": \"Sông Nile\",\r\n"
        		+ "      \"isCorrect\": true,\r\n"
        		+ "      \"timeSpentSeconds\": 8,\r\n"
        		+ "      \"timeSpentExpectedSeconds\": 10,\r\n"
        		+ "      \"questionLevel\": 1,\r\n"
        		+ "      \"conceptTags\": [\"Sông ngòi\", \"Thế giới\"]\r\n"
        		+ "    },\r\n"
        		+ "    {\r\n"
        		+ "      \"questionId\": 102,\r\n"
        		+ "      \"question\": \"Đỉnh núi cao nhất thế giới là đỉnh nào?\",\r\n"
        		+ "      \"topic\": \"Địa lý\",\r\n"
        		+ "      \"questionType\": \"SHORT_ANSWER\",\r\n"
        		+ "      \"answerOptions\": null,\r\n"
        		+ "      \"userResponse\": \"Everest\",\r\n"
        		+ "      \"correctAnswer\": \"Everest\",\r\n"
        		+ "      \"isCorrect\": true,\r\n"
        		+ "      \"timeSpentSeconds\": 10,\r\n"
        		+ "      \"timeSpentExpectedSeconds\": 10,\r\n"
        		+ "      \"questionLevel\": 1,\r\n"
        		+ "      \"conceptTags\": [\"Núi\", \"Thế giới\", \"Châu Á\"]\r\n"
        		+ "    },\r\n"
        		+ "    {\r\n"
        		+ "      \"questionId\": 201,\r\n"
        		+ "      \"question\": \"Sa mạc nào lớn nhất thế giới (tính cả sa mạc nóng và lạnh)?\",\r\n"
        		+ "      \"topic\": \"Địa lý\",\r\n"
        		+ "      \"questionType\": \"multiple_choice\",\r\n"
        		+ "      \"answerOptions\": [\"Sahara\", \"Gobi\", \"Nam Cực\", \"Ả Rập\"],\r\n"
        		+ "      \"userResponse\": \"Sahara\",\r\n"
        		+ "      \"correctAnswer\": \"Nam Cực\",\r\n"
        		+ "      \"isCorrect\": false,\r\n"
        		+ "      \"timeSpentSeconds\": 12,\r\n"
        		+ "      \"timeSpentExpectedSeconds\": 25,\r\n"
        		+ "      \"questionLevel\": 2,\r\n"
        		+ "      \"conceptTags\": [\"Sa mạc\", \"Thế giới\", \"Khí hậu\"]\r\n"
        		+ "    },\r\n"
        		+ "    {\r\n"
        		+ "      \"questionId\": 301,\r\n"
        		+ "      \"question\": \"Kênh đào Suez nối liền hai biển nào?\",\r\n"
        		+ "      \"topic\": \"Địa lý\",\r\n"
        		+ "      \"questionType\": \"SHORT_ANSWER\",\r\n"
        		+ "      \"answerOptions\": null,\r\n"
        		+ "      \"userResponse\": \"Địa Trung Hải và Biển Đen\",\r\n"
        		+ "      \"correctAnswer\": \"Địa Trung Hải và Biển Đỏ\",\r\n"
        		+ "      \"isCorrect\": false,\r\n"
        		+ "      \"timeSpentSeconds\": 45,\r\n"
        		+ "      \"timeSpentExpectedSeconds\": 40,\r\n"
        		+ "      \"questionLevel\": 3,\r\n"
        		+ "      \"conceptTags\": [\"Kênh đào\", \"Giao thông\", \"Địa lý kinh tế\"]\r\n"
        		+ "    },\r\n"
        		+ "    {\r\n"
        		+ "      \"questionId\": 202,\r\n"
        		+ "      \"question\": \"Thành phố nào của Nhật Bản bị ném bom nguyên tử đầu tiên trong Thế chiến II?\",\r\n"
        		+ "      \"topic\": \"Địa lý\",\r\n"
        		+ "      \"questionType\": \"SHORT_ANSWER\",\r\n"
        		+ "      \"answerOptions\": null,\r\n"
        		+ "      \"userResponse\": \"Hiroshima\",\r\n"
        		+ "      \"correctAnswer\": \"Hiroshima\",\r\n"
        		+ "      \"isCorrect\": true,\r\n"
        		+ "      \"timeSpentSeconds\": 20,\r\n"
        		+ "      \"timeSpentExpectedSeconds\": 20,\r\n"
        		+ "      \"questionLevel\": 2,\r\n"
        		+ "      \"conceptTags\": [\"Lịch sử địa lý\", \"Nhật Bản\", \"Châu Á\"]\r\n"
        		+ "    },\r\n"
        		+ "    {\r\n"
        		+ "      \"questionId\": 203,\r\n"
        		+ "      \"question\": \"Dãy núi nào là ranh giới tự nhiên giữa Châu Âu và Châu Á?\",\r\n"
        		+ "      \"topic\": \"Địa lý\",\r\n"
        		+ "      \"questionType\": \"multiple_choice\",\r\n"
        		+ "      \"answerOptions\": [\"Alps\", \"Himalaya\", \"Ural\", \"Andes\"],\r\n"
        		+ "      \"userResponse\": \"Ural\",\r\n"
        		+ "      \"correctAnswer\": \"Ural\",\r\n"
        		+ "      \"isCorrect\": true,\r\n"
        		+ "      \"timeSpentSeconds\": 25,\r\n"
        		+ "      \"timeSpentExpectedSeconds\": 25,\r\n"
        		+ "      \"questionLevel\": 2,\r\n"
        		+ "      \"conceptTags\": [\"Ranh giới\", \"Châu Lục\", \"Dãy núi\"]\r\n"
        		+ "    },\r\n"
        		+ "    {\r\n"
        		+ "      \"questionId\": 103,\r\n"
        		+ "      \"question\": \"Quốc gia nào có diện tích lớn nhất thế giới?\",\r\n"
        		+ "      \"topic\": \"Địa lý\",\r\n"
        		+ "      \"questionType\": \"SHORT_ANSWER\",\r\n"
        		+ "      \"answerOptions\": null,\r\n"
        		+ "      \"userResponse\": \"Nga\",\r\n"
        		+ "      \"correctAnswer\": \"Nga\",\r\n"
        		+ "      \"isCorrect\": true,\r\n"
        		+ "      \"timeSpentSeconds\": 6,\r\n"
        		+ "      \"timeSpentExpectedSeconds\": 10,\r\n"
        		+ "      \"questionLevel\": 1,\r\n"
        		+ "      \"conceptTags\": [\"Quốc gia\", \"Thế giới\"]\r\n"
        		+ "    },\r\n"
        		+ "    {\r\n"
        		+ "      \"questionId\": 302,\r\n"
        		+ "      \"question\": \"Hồ nước ngọt lớn nhất thế giới về diện tích là hồ nào?\",\r\n"
        		+ "      \"topic\": \"Địa lý\",\r\n"
        		+ "      \"questionType\": \"SHORT_ANSWER\",\r\n"
        		+ "      \"answerOptions\": null,\r\n"
        		+ "      \"userResponse\": \"Hồ Baikal\",\r\n"
        		+ "      \"correctAnswer\": \"Hồ Superior\",\r\n"
        		+ "      \"isCorrect\": false,\r\n"
        		+ "      \"timeSpentSeconds\": 50,\r\n"
        		+ "      \"timeSpentExpectedSeconds\": 40,\r\n"
        		+ "      \"questionLevel\": 3,\r\n"
        		+ "      \"conceptTags\": [\"Hồ\", \"Nước ngọt\", \"Thế giới\"]\r\n"
        		+ "    },\r\n"
        		+ "    {\r\n"
        		+ "      \"questionId\": 204,\r\n"
        		+ "      \"question\": \"Vành đai lửa Thái Bình Dương nổi tiếng vì điều gì?\",\r\n"
        		+ "      \"topic\": \"Địa lý\",\r\n"
        		+ "      \"questionType\": \"multiple_choice\",\r\n"
        		+ "      \"answerOptions\": [\"Các rạn san hô lớn\", \"Dòng hải lưu nóng\", \"Hoạt động núi lửa và động đất\", \"Các mỏ dầu lớn\"],\r\n"
        		+ "      \"userResponse\": \"Hoạt động núi lửa và động đất\",\r\n"
        		+ "      \"correctAnswer\": \"Hoạt động núi lửa và động đất\",\r\n"
        		+ "      \"isCorrect\": true,\r\n"
        		+ "      \"timeSpentSeconds\": 18,\r\n"
        		+ "      \"timeSpentExpectedSeconds\": 20,\r\n"
        		+ "      \"questionLevel\": 2,\r\n"
        		+ "      \"conceptTags\": [\"Địa chất\", \"Thái Bình Dương\", \"Núi lửa\"]\r\n"
        		+ "    },\r\n"
        		+ "    {\r\n"
        		+ "      \"questionId\": 205,\r\n"
        		+ "      \"question\": \"Ngoài Nga, quốc gia nào cũng nằm trên cả hai châu lục là Châu Âu và Châu Á?\",\r\n"
        		+ "      \"topic\": \"Địa lý\",\r\n"
        		+ "      \"questionType\": \"d\",\r\n"
        		+ "      \"answerOptions\": null,\r\n"
        		+ "      \"userResponse\": \"Ai Cập\",\r\n"
        		+ "      \"correctAnswer\": \"Thổ Nhĩ Kỳ\",\r\n"
        		+ "      \"isCorrect\": false,\r\n"
        		+ "      \"timeSpentSeconds\": 30,\r\n"
        		+ "      \"timeSpentExpectedSeconds\": 30,\r\n"
        		+ "      \"questionLevel\": 2,\r\n"
        		+ "      \"conceptTags\": [\"Quốc gia\", \"Châu Lục\", \"Ranh giới\"]\r\n"
        		+ "    }\r\n"
        		+ "  ]\r\n"
        		+ "}";

               
        		UserEvaluation evaluation =agent.analysis(userInput);
        		System.out.println(evaluation.getAverageScore());
              	}
	
}
