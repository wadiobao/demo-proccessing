package com.example.demo.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Utility to log AI instructions and prompts to physical files for debugging and auditing.
 */
@Component
@Slf4j
public class AiDebugLogger {

    private static final String LOG_DIR = "logs/ai-prompts";
    private static final DateTimeFormatter FILE_NAME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    public void logAiCall(String type, String instruction, String prompt) {
        try {
            Path directory = Paths.get(LOG_DIR);
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }

            String fileName = String.format("%s_%s.txt", LocalDateTime.now().format(FILE_NAME_FORMATTER), type);
            Path filePath = directory.resolve(fileName);

            StringBuilder sb = new StringBuilder();
            sb.append("=================================================================\n");
            sb.append("AI CALL LOG - ").append(LocalDateTime.now()).append("\n");
            sb.append("TYPE: ").append(type).append("\n");
            sb.append("=================================================================\n\n");
            
            sb.append("--- SYSTEM INSTRUCTION ---\n");
            sb.append(instruction).append("\n\n");
            
            sb.append("--- USER PROMPT ---\n");
            sb.append(prompt).append("\n\n");
            
            sb.append("========================= END OF LOG =========================\n");

            Files.writeString(filePath, sb.toString(), StandardOpenOption.CREATE);
            log.info("[DEBUG] AI Prompt logged to: {}", filePath.toAbsolutePath());

        } catch (IOException e) {
            log.error("Failed to log AI prompt to file: {}", e.getMessage());
        }
    }
}
