package com.example.demo.sql.service;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.demo.sql.dto.ReadingProgressRequest;
import com.example.demo.sql.dto.ReadingProgressResponse;
import com.example.demo.sql.repository.UserRepository;
import com.example.demo.sql.service.iservice.IReadingProgressService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Triển khai dịch vụ quản lý tiến trình đọc sách sử dụng Redis.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReadingProgressService implements IReadingProgressService {

    private final RedisTemplate<String, String> redisTemplate;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    private static final String KEY_PREFIX = "reading_progress:";
    private static final long TTL_DAYS = 3;

    /**
     * Lấy ID người dùng hiện tại từ SecurityContext.
     */
    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username))
                .getId();
    }

    private String buildKey(Long userId, Long pdfId) {
        return KEY_PREFIX + userId + ":" + pdfId;
    }

    @Override
    public void saveProgress(ReadingProgressRequest request) {
        Long userId = getCurrentUserId();
        String key = buildKey(userId, request.getPdfId());

        ReadingProgressResponse progress = ReadingProgressResponse.builder()
                .pdfId(request.getPdfId())
                .scrollPercent(request.getScrollPercent())
                .lastPage(request.getLastPage())
                .build();

        try {
            String jsonValue = objectMapper.writeValueAsString(progress);
            redisTemplate.opsForValue().set(key, jsonValue, TTL_DAYS, TimeUnit.DAYS);
            log.info("Saved progress for user {} on pdf {}: {}", userId, request.getPdfId(), jsonValue);
        } catch (JsonProcessingException e) {
            log.error("Error serializing reading progress", e);
            throw new RuntimeException("Could not save reading progress");
        }
    }

    @Override
    public ReadingProgressResponse getProgress(Long pdfId) {
        Long userId = getCurrentUserId();
        String key = buildKey(userId, pdfId);

        String jsonValue = redisTemplate.opsForValue().get(key);
        if (jsonValue == null) {
            log.info("No progress found for user {} on pdf {}", userId, pdfId);
            return null;
        }

        try {
            return objectMapper.readValue(jsonValue, ReadingProgressResponse.class);
        } catch (JsonProcessingException e) {
            log.error("Error deserializing reading progress", e);
            return null;
        }
    }
}
