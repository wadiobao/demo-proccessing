package com.example.demo.modules.identity.infrastructure.adapter;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.example.demo.modules.identity.domain.port.VerificationPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisVerificationAdapter implements VerificationPort {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void store(String key, String value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    @Override
    public Optional<String> get(String key) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key));
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }
}
