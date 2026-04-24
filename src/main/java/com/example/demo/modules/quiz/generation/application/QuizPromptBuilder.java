package com.example.demo.modules.quiz.generation.application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.example.demo.modules.quiz.shared.domain.model.QuizConfig;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * Service to build modular LLM prompts by assembling specialized instruction blocks.
 * Separates System Instructions (Rules) from User Messages (Data).
 */
@Component
@Slf4j
public class QuizPromptBuilder {

    @Value("${demo.instruction.base.quality}")
    private String baseQualityPath;

    @Value("${demo.instruction.base.bloom}")
    private String baseBloomPath;

    @Value("${demo.instruction.base.schema}")
    private String baseSchemaPath;

    @Value("${demo.instruction.mode.standard}")
    private String modeStandardPath;

    @Value("${demo.instruction.mode.personalized}")
    private String modePersonalizedPath;


    @Value("${demo.instruction.mode.identify}")
    private String modeIdentifyPath;

    private String baseQuality;
    private String baseBloom;
    private String baseSchema;
    private String modeStandard;
    private String modePersonalized;
    private String modeIdentify;

    @PostConstruct
    public void init() {
        try {
            this.baseQuality = loadResource(baseQualityPath);
            this.baseBloom = loadResource(baseBloomPath);
            this.baseSchema = loadResource(baseSchemaPath);
            this.modeStandard = loadResource(modeStandardPath);
            this.modePersonalized = loadResource(modePersonalizedPath);
            this.modeIdentify = loadResource(modeIdentifyPath);
            log.info("QuizPromptBuilder initialized with modular instructions.");
        } catch (IOException e) {
            log.error("Failed to load modular instruction files: {}", e.getMessage());
        }
    }

    private String loadResource(String path) throws IOException {
        InputStream is = new ClassPathResource(path).getInputStream();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            for (String line; (line = reader.readLine()) != null;) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Context containing both system instructions and user-specific prompt data.
     */
    public record PromptContext(String instruction, String userMessage) {}

    public PromptContext buildPersonalizedPrompt(QuizConfig config, List<String> chunks) {
        String instruction = new StringBuilder()
                .append(modePersonalized).append("\n\n")
                .append(baseQuality).append("\n\n")
                .append(baseBloom).append("\n\n")
                .append(baseSchema)
                .toString();

        String params = String.format(Locale.US,
                "[[bloom_allocation]]:%s\n[[min_difficulty]]:%.2f\n[[max_difficulty]]:%.2f\n[[image]]:%d\n[[language]]:%s\n",
                config.getBloomAllocation() != null ? config.getBloomAllocation() : "15 questions (Balanced distribution)",
                config.getMinDifficulty() != null ? config.getMinDifficulty() : -1.0,
                config.getMaxDifficulty() != null ? config.getMaxDifficulty() : 1.0,
                config.getImgQuest(),
                config.getLanguage());

        StringBuilder chunkBlock = new StringBuilder("=== KNOWLEDGE CHUNKS ===\n");
        for (int i = 0; i < chunks.size(); i++) {
            chunkBlock.append(String.format("\n--- Chunk %d ---\n%s\n", i + 1, chunks.get(i)));
        }

        String userMessage = params + "\n" + chunkBlock.toString();
        return new PromptContext(instruction, userMessage);
    }

    public PromptContext buildStandardPrompt(QuizConfig config, String pdfText, String relatedContext) {
        String instruction = new StringBuilder()
                .append(modeStandard).append("\n\n")
                .append(baseQuality).append("\n\n")
                .append(baseBloom).append("\n\n")
                .append(baseSchema)
                .toString();

        String params = String.format(Locale.US,
                "[[count]]:%d\n[[level]]:%d\n[[mode]]:%d\n[[image]]:%d\n[[language]]:%s\n",
                config.getQuestionCount(),
                config.getLevel(),
                config.getType(),
                config.getImgQuest(),
                config.getLanguage());

        String userMessage = params + "\n\n=== DOCUMENT CONTENT ===\n" + pdfText;
        if (relatedContext != null) {
            userMessage += "\n\n=== RELATED CONTEXT ===\n" + relatedContext;
        }

        return new PromptContext(instruction, userMessage);
    }


    public PromptContext buildIdentificationPrompt(String pdfText) {
        String instruction = new StringBuilder()
                .append(modeIdentify).append("\n\n")
                .append(baseSchema)
                .toString();

        String userMessage = "=== DOCUMENT CONTENT ===\n" + pdfText;
        return new PromptContext(instruction, userMessage);
    }
}
