package com.example.demo.modules.document.progress.infrastructure.adapter;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.example.demo.modules.document.progress.api.dto.ReadingProgressRequest;
import com.example.demo.modules.document.progress.api.dto.ReadingProgressResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReadingProgressCacheAdapter {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String PROGRESS_KEY_PREFIX = "pdf_progress:";

    public ReadingProgressResponse saveProgress(Long userId, ReadingProgressRequest request) {
        String key = PROGRESS_KEY_PREFIX + userId + ":" + request.getPdfId();
        ReadingProgressResponse response = ReadingProgressResponse.builder()
                .pdfId(request.getPdfId())
                .userId(userId)
                .scrollPercentage(request.getScrollPercentage())
                .lastPage(null)
                .timestamp(System.currentTimeMillis())
                .build();

        try {
            String json = objectMapper.writeValueAsString(response);
            // Lưu trữ 30 ngày
            redisTemplate.opsForValue().set(key, json, 30, TimeUnit.DAYS);
        } catch (JsonProcessingException e) {
            log.error("Error serializing reading progress", e);
        }

        return response;
    }

    public ReadingProgressResponse getProgress(Long userId, Long pdfId) {
        String key = PROGRESS_KEY_PREFIX + userId + ":" + pdfId;
        String json = redisTemplate.opsForValue().get(key);

        if (json != null) {
            try {
                return objectMapper.readValue(json, ReadingProgressResponse.class);
            } catch (JsonProcessingException e) {
                log.error("Error deserializing reading progress", e);
            }
        }

        return ReadingProgressResponse.builder()
                .pdfId(pdfId)
                .userId(userId)
                .scrollPercentage(0.0)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
