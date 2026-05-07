package com.example.demo.modules.community.forum.application.usecase.command;

import java.time.Duration;
import java.util.ArrayList;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.demo.modules.community.forum.api.dto.FormSession;
import com.google.gson.Gson;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManageFormSessionUseCase {

    private final RedisTemplate<String, String> redisTemplate;
    private final Gson gson;

    private static final String SESSION_KEY_PREFIX = "form_session:";
    private static final long SESSION_TTL_SECONDS = 900;

    public String startSession(String username) {
        String sessionId = UUID.randomUUID().toString();
        FormSession session = FormSession.builder()
                .sessionId(sessionId)
                .ownerName(username)
                .createdAt(System.currentTimeMillis())
                .questions(new ArrayList<>())
                .build();

        redisTemplate.opsForValue().set(SESSION_KEY_PREFIX + sessionId, gson.toJson(session),
                Duration.ofSeconds(SESSION_TTL_SECONDS));

        log.info("Started form creation session {} for user {}", sessionId, username);
        return sessionId;
    }

    public FormSession getSession(String sessionId, String username) {
        String json = redisTemplate.opsForValue().get(SESSION_KEY_PREFIX + sessionId);
        if (json == null) return null;
        
        FormSession session = gson.fromJson(json, FormSession.class);
        if (username != null && !username.equals(session.getOwnerName())) {
            throw new RuntimeException("Unauthorized: This session belongs to another user.");
        }
        return session;
    }

    public void updateQuestions(String sessionId, String username, java.util.List<com.example.demo.modules.quiz.shared.domain.model.Question> questions) {
        String raw = redisTemplate.opsForValue().get(SESSION_KEY_PREFIX + sessionId);
        if (raw == null) throw new RuntimeException("Session not found or expired.");

        FormSession session = gson.fromJson(raw, FormSession.class);
        if (!username.equals(session.getOwnerName())) {
            throw new RuntimeException("Unauthorized: This session belongs to another user.");
        }

        session.setQuestions(questions);
        redisTemplate.opsForValue().set(SESSION_KEY_PREFIX + sessionId, gson.toJson(session),
                Duration.ofSeconds(SESSION_TTL_SECONDS));
    }

    public void discardSession(String sessionId, String username) {
        String raw = redisTemplate.opsForValue().get(SESSION_KEY_PREFIX + sessionId);
        if (raw != null) {
            FormSession session = gson.fromJson(raw, FormSession.class);
            if (username.equals(session.getOwnerName())) {
                redisTemplate.delete(SESSION_KEY_PREFIX + sessionId);
                log.info("Discarded session {} for user {}", sessionId, username);
            }
        }
    }
}
