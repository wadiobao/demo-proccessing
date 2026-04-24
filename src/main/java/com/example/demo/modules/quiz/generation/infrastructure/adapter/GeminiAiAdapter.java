package com.example.demo.modules.quiz.generation.infrastructure.adapter;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.demo.constants.Constants;
import com.example.demo.modules.quiz.generation.infrastructure.port.AiGenerationPort;
import com.example.demo.modules.quiz.shared.domain.model.Question;
import com.example.demo.utils.CloudinaryUtils;
import com.example.demo.utils.HandleTextFromGeminiUtils;
import com.example.demo.utils.PromptSanitizer;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Adapter for Google Gemini AI implementation of AiGenerationPort.
 * Now supports separate System Instructions and User Prompts.
 */
@Component("quizGeminiAiAdapter")
@RequiredArgsConstructor
@Slf4j
public class GeminiAiAdapter implements AiGenerationPort {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.key.image}")
    private String imageGeminiApiKey;

    private final String MODEL = Constants.Api.GEMINI_MODEL;
    private final String IMAGE_MODEL = Constants.Api.IMAGE_MODEL;

    private final HandleTextFromGeminiUtils handleTextFromGeminiUtils;
    private final CloudinaryUtils cloudinaryUtils;
    private final PromptSanitizer promptSanitizer;
    private final com.example.demo.utils.AiDebugLogger aiDebugLogger;

    @Override
    public AiResponse generateQuestions(String instruction, String prompt) {
        return callGemini(instruction, prompt);
    }

    @Override
    public AiResponse generateIdentifiedQuestions(String instruction, String prompt) {
        return callGemini(instruction, prompt);
    }

    @Override
    public AiResponse reGenerateQuestions(String instruction, String prompt) {
        return callGemini(instruction, prompt);
    }

    @Override
    public String[] generateImage(String prompt, int questionId) {
        try {
            aiDebugLogger.logAiCall("IMAGE_GEN", "Image Generation Request", prompt);
            Client client = new Client.Builder().apiKey(imageGeminiApiKey).build();
            GenerateContentConfig config = GenerateContentConfig.builder()
                    .responseModalities(List.of("TEXT", "IMAGE"))
                    .build();

            GenerateContentResponse response = client.models.generateContent(IMAGE_MODEL, prompt, config);
            String imgBase64 = HandleTextFromGeminiUtils.extractDataFromGemini(response);
            return cloudinaryUtils.saveImageFromBase64(imgBase64, questionId + ".png");
        } catch (Exception e) {
            log.error("Gemini image generation failed: {}", e.getMessage());
            return null;
        }
    }

    private AiResponse callGemini(String instruction, String userPrompt) {
        try {
            // Log the call for debugging
            aiDebugLogger.logAiCall("QUIZ_GEN", instruction, userPrompt);

            Client client = new Client.Builder().apiKey(geminiApiKey).build();
            
            // Set System Instruction
            Content sysInstruction = Content.builder()
                    .parts(List.of(Part.builder().text(instruction).build()))
                    .build();

            GenerateContentConfig config = GenerateContentConfig.builder()
                    .systemInstruction(sysInstruction)
                    .responseMimeType("application/json")
                    .build();

            String sanitizedPrompt = promptSanitizer.sanitize(userPrompt);
            GenerateContentResponse response = client.models.generateContent(MODEL, sanitizedPrompt, config);
            
            List<Question> parsedList = handleTextFromGeminiUtils.parseQuestionsV4(response.text());
            return new AiResponse(response.text(), parsedList);
            
        } catch (Exception e) {
            log.error("Gemini API call failed: {}", e.getMessage());
            throw new RuntimeException("AI Generation Error", e);
        }
    }
}
