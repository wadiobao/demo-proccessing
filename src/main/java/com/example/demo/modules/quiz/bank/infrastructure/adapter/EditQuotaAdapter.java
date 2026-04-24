package com.example.demo.modules.quiz.bank.infrastructure.adapter;

import java.time.Duration;
import java.time.LocalDate;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.example.demo.modules.quiz.bank.infrastructure.port.EditQuotaPort;

import lombok.RequiredArgsConstructor;

/**
 * Adapter for managing user edit quotas using Redis.
 */
@Component
@RequiredArgsConstructor
public class EditQuotaAdapter implements EditQuotaPort {

    private final RedisTemplate<String, String> redisTemplate;

    private static final int MAX_DAILY_EDITS = 5;
    private static final String QUOTA_KEY_PREFIX = "quota:question_bank:edit:";

    @Override
    public boolean hasExceededQuota(String username) {
        return getCurrentCount(username) >= MAX_DAILY_EDITS;
    }

    @Override
    public void incrementQuota(String username) {
        String quotaKey = getQuotaKey(username);
        redisTemplate.opsForValue().increment(quotaKey);
        redisTemplate.expire(quotaKey, Duration.ofDays(1));
    }

    @Override
    public int getCurrentCount(String username) {
        String quotaKey = getQuotaKey(username);
        String currentCountStr = redisTemplate.opsForValue().get(quotaKey);
        return (currentCountStr != null) ? Integer.parseInt(currentCountStr) : 0;
    }

    private String getQuotaKey(String username) {
        return QUOTA_KEY_PREFIX + username + ":" + LocalDate.now();
    }
}
