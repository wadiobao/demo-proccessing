package com.example.demo.service.quiz;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.example.demo.constants.Constants;
import com.example.demo.dto.question.Question;
import com.example.demo.utils.FileGeneratorUtils;
import com.example.demo.utils.HandleTextFromGeminiUtils;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;

import lombok.AllArgsConstructor;
import lombok.Data;

@Service
public class GeminiAIService {
    
    @Value("${gemini.api.key}")
    private String geminiApiKey;
    
    @Value("${gemini.api.key.image}")
    private String imageGeminiApiKey;  
    
    private final String MODEL = Constants.Api.GEMINI_MODEL;
    private final String IMAGE_MODEL = Constants.Api.IMAGE_MODEL;
    @Value("${demo.instruction.path}")
    private String instructionPath;
    
    @Value("${demo.instruction.regen.path}")
    private String instructionRegenPath;
    
    
    @Autowired
    private HandleTextFromGeminiUtils handleTextFromGeminiUtils;
    
    @Autowired
    private FileGeneratorUtils fileGeneratorUtils;
    
    private List<String> systemInstuction = new ArrayList<String>();
    
    @Data
    @AllArgsConstructor
    public static class GeminiResponse {
        private String text;
        private List<Question> questions;
        
        public GeminiResponse(String text) {
        	this.text = text;
		}
    }
    
    public GeminiResponse generateQuestionWithGemini(String userPrompt) throws IOException {
        makeInstruction();
        Client client = new Client.Builder().apiKey(geminiApiKey).build();
        List<Part> parts = new ArrayList<Part>();
        Part part = Part.builder().text(systemInstuction.toString()).build();
        parts.add(part);
        Content content = Content.builder().parts(parts).build();
        
        GenerateContentConfig config = GenerateContentConfig.builder().systemInstruction(content).build();
        
        GenerateContentResponse response = client.models.generateContent(MODEL, userPrompt, config);
        List<Question> parsedList = handleTextFromGeminiUtils.parseQuestionsV4(response.text());
        
        return new GeminiResponse(response.text(), parsedList);
    }
    
    public GeminiResponse reGenerateQuestionWithGemini(String userPrompt) throws IOException {
        makeReGenInstruction();
        Client client = new Client.Builder().apiKey(geminiApiKey).build();
        List<Part> parts = new ArrayList<Part>();
        Part part = Part.builder().text(systemInstuction.toString()).build();
        parts.add(part);
        Content content = Content.builder().parts(parts).build();
        
        GenerateContentConfig config = GenerateContentConfig.builder().systemInstruction(content).build();
        
        GenerateContentResponse response = client.models.generateContent(MODEL, userPrompt, config);
        List<Question> parsedList = handleTextFromGeminiUtils.parseQuestionsV4(response.text());
        
        return new GeminiResponse(response.text(), parsedList);
    }
    
    public String[] generateImageWithGemini(String imgPrompt,int id) throws IOException {
        Client client = new Client.Builder().apiKey(imageGeminiApiKey).build();
       
        
        GenerateContentConfig config = GenerateContentConfig.builder().responseModalities(List.of("TEXT","IMAGE")).build();
        
        GenerateContentResponse response = client.models.generateContent(IMAGE_MODEL, imgPrompt, config);
        //System.out.println(response.toJson());
        
        String imgBase64 = HandleTextFromGeminiUtils.extractDataFromGemini(response);
        
        String imgAttributes[] = fileGeneratorUtils.saveImageFromBase64(imgBase64,id+".png");
        
        
        return imgAttributes;
    
    }

    
    private void makeInstruction() throws IOException {
        InputStream resource = new ClassPathResource(instructionPath).getInputStream();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))){
            for(String line; (line = reader.readLine())!=null;) {
                systemInstuction.add(line);
            }
        }catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    
    
    private void makeReGenInstruction() throws IOException {
        InputStream resource = new ClassPathResource(instructionRegenPath).getInputStream();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))){
            for(String line; (line = reader.readLine())!=null;) {
                systemInstuction.add(line);
            }
        }catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    
    
    public static void main(String[] args) throws IOException {
//    	GeminiAIService geminiResponse = new GeminiAIService();
//    	geminiResponse.generateImageWithGemini("generate 2 separate picture of dogs");
    	System.out.println(Constants.FilePaths.IMAGE_TEMP+1+".png");
	}
} 