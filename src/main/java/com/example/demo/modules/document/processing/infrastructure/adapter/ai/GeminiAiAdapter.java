package com.example.demo.modules.document.processing.infrastructure.adapter.ai;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.example.demo.modules.document.processing.application.port.output.AiAnalysisPort;
import com.example.demo.constants.Constants;
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
 * Adapter for interactions with Google Gemini AI for document analysis.
 * 
 * <p>
 * Triển khai các khả năng phân tích thông minh như trích xuất keyword,
 * nhận diện chủ đề và tóm tắt nội dung thông qua Gemini API.
 */
@Component("documentGeminiAiAdapter")
@RequiredArgsConstructor
@Slf4j
public class GeminiAiAdapter implements AiAnalysisPort {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.key.topic}")
    private String topicGeminiApiKey;

    @Value("${demo.instruction.topic-tags.path}")
    private String topicAndTagsPath;

    private final HandleTextFromGeminiUtils handleTextFromGeminiUtils;
    private final PromptSanitizer promptSanitizer;

    @Override
    public String analyze(String prompt) {
        try {
            Client client = new Client.Builder().apiKey(geminiApiKey).build();
            GenerateContentResponse response = client.models.generateContent(Constants.Api.GEMINI_MODEL, prompt, null);
            return response.text();
        } catch (Exception e) {
            log.error("Gemini analysis error: {}", e.getMessage());
            return "Error during AI analysis";
        }
    }

    @Override
    public List<String> extractKeywords(String content, int count) {
        try {
            // Reusing topic detection logic for keyword extraction
            List<String> instructions = loadInstruction(topicAndTagsPath);
            String sanitizedContent = promptSanitizer.sanitize(content);
            
            String enhancedPrompt = String.format(
                "Nhiệm vụ: Trích xuất tối đa %d từ khóa (keywords) quan trọng nhất từ văn bản sau.\n\nNội dung:\n%s",
                count, sanitizedContent
            );

            Client client = new Client.Builder().apiKey(topicGeminiApiKey).build();
            List<Part> parts = new ArrayList<>();
            parts.add(Part.builder().text(instructions.toString()).build());
            
            GenerateContentConfig config = GenerateContentConfig.builder()
                    .systemInstruction(Content.builder().parts(parts).build())
                    .responseMimeType("application/json")
                    .build();

            GenerateContentResponse response = client.models.generateContent(Constants.Api.GEMINI_MODEL, enhancedPrompt, config);
            // This is a simplification; in a real scenario we'd parse the specific JSON structure
            return handleTextFromGeminiUtils.parseTopicAndTags(response.text()).getTags();
        } catch (Exception e) {
            log.error("Keyword extraction error: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<String> loadInstruction(String path) throws IOException {
        List<String> instructions = new ArrayList<>();
        InputStream resource = new ClassPathResource(path).getInputStream();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))) {
            for (String line; (line = reader.readLine()) != null;) {
                instructions.add(line);
            }
        }
        return instructions;
    }
}
