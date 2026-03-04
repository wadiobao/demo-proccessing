package com.example.demo.utils;

import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility to sanitize user-provided content against prompt injection attacks.
 * 
 * <p>
 * Ngăn chặn các cuộc tấn công Prompt Injection bằng cách lọc các từ khóa
 * điều khiển AI từ nội dung do người dùng tải lên.
 */
@Component
@Slf4j
public class PromptSanitizer {

    private static final int MAX_PROMPT_CONTENT_LENGTH = 50_000;

    /**
     * Sanitizes raw text to prevent AI instruction overrides.
     * 
     * @param text input content from user
     * @return cleaned text safe for prompt injection
     */
    public String sanitize(String text) {
        if (text == null)
            return "";

        // 1. Length limit to prevent token exhaustion or DOS-like costs
        String sanitized = text;
        if (sanitized.length() > MAX_PROMPT_CONTENT_LENGTH) {
            log.warn("Truncating extremely long user content ({} chars) for prompt safety.", sanitized.length());
            sanitized = sanitized.substring(0, MAX_PROMPT_CONTENT_LENGTH);
        }

        // 2. Neutralize typical imperative instruction keywords
        // Note: We use case-insensitive replacement with filtered markers
        // to break the semantic structure of an injection attempt.
        sanitized = sanitized.replaceAll(
                "(?i)(ignore|override|system|instruction|prompt|forget|delete|remove|reveal|show|return)",
                "[FILTERED]");

        return sanitized;
    }
}
