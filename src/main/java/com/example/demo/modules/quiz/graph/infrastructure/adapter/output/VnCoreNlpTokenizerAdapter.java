package com.example.demo.modules.quiz.graph.infrastructure.adapter.output;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;
import com.example.demo.modules.quiz.graph.application.port.output.TokenizerPort;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class VnCoreNlpTokenizerAdapter implements TokenizerPort {

    @Override
    public Object process(String text) {
        log.info("Processing text using fallback Tokenizer (Whitespace split)...");
        
        List<String> tokens = new ArrayList<>();
        List<Entity> entities = new ArrayList<>();

        if (text != null && !text.isBlank()) {
            // Fallback: Simple split by whitespace and punctuation removal
            String[] words = text.replaceAll("[^\\p{L}\\p{N}\\s]", "").split("\\s+");
            tokens.addAll(Arrays.asList(words));
        }

        return new TokenizedDocument(tokens, entities);
    }

    // Dummy classes to fix compilation errors
    @Data
    @AllArgsConstructor
    public static class Entity {
        private String text;
        private String label;
    }

    @Data
    @AllArgsConstructor
    public static class TokenizedDocument {
        private List<String> tokens;
        private List<Entity> entities;
    }
}
