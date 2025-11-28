package com.example.demo.agent.agents;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.question.Question;
import com.example.demo.utils.HandleTextFromGeminiUtils;
import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;

import io.reactivex.rxjava3.core.Flowable;

public class ReGenerateAgent {
	private String USER_ID = null;
	private static String NAME = "ReGenerateAgent";
	private static final String MODEL_NAME = "gemini-2.5-flash";
	private InMemoryRunner runner = null;
	private Session session = null;
	
	@Value("${demo.instruction.regen.path}")
	private static String instruction;
	
	private HandleTextFromGeminiUtils handleTextFromGeminiUtils = new HandleTextFromGeminiUtils();
	
	public static BaseAgent initAgent() {
		return LlmAgent.builder()
                .model(MODEL_NAME)
                .name(NAME)
                .description("Re-generate agent")
                .instruction(instruction)
                .outputKey("re_gen_result")
                .build();
	}
	
	public List<Question> generate(MultipartFile file) {
		String fileContext = file.getContentType();
		byte[] bytes;
		try {
			bytes = file.getBytes();
			Content userMsg = Content.fromParts(Part.fromBytes(bytes, fileContext));
	        Flowable<Event> events = runner.runAsync(USER_ID, session.id(), userMsg);
	        StringBuilder raw = new StringBuilder();
	        events.blockingForEach(event -> raw.append(event.stringifyContent())); //raw.append(event.stringifyContent()));
	        String clean = raw.substring(7, raw.length()-3);
	        List<Question> parsedList = handleTextFromGeminiUtils.parseQuestionsV4(raw.toString());
	        return parsedList;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
