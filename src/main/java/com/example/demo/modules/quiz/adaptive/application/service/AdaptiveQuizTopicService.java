package com.example.demo.modules.quiz.adaptive.application.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.UserResourceMongoEntity;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.repository.UserResourceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing UserResource (Topic) lifecycle and file association.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdaptiveQuizTopicService {

    private final UserResourceRepository userResourceRepository;

    /**
     * Retrieves or creates a UserResource for a given topic and associates new file IDs.
     */
    public UserResourceMongoEntity syncTopicResource(String username, String topic, List<String> newMetadataIds, int sessionSize) {
        log.info("Syncing topic resource for user {} and topic {}", username, topic);
        
        UserResourceMongoEntity userResource = userResourceRepository.findByUserNameAndTopic(username, topic)
                .orElseGet(() -> UserResourceMongoEntity.builder()
                        .userName(username)
                        .topic(topic)
                        .theta(0.0)
                        .b(0.0)
                        .sessionSize(sessionSize)
                        .history(new ArrayList<>())
                        .thetaHistory(new ArrayList<>())
                        .contentIds(new ArrayList<>())
                        .build());

        // Link all individual metadata IDs to the topic
        for (String id : newMetadataIds) {
            if (!userResource.getContentIds().contains(id)) {
                userResource.getContentIds().add(id);
            }
        }

        if (sessionSize > 0) {
            userResource.setSessionSize(sessionSize);
        }

        return userResourceRepository.save(userResource);
    }

    /**
     * Retrieves a UserResource by ID and validates ownership.
     */
    public UserResourceMongoEntity getTopicResource(String topicId, String username) {
        UserResourceMongoEntity userResource = userResourceRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found"));

        if (!userResource.getUserName().equals(username)) {
            throw new SecurityException("You do not have permission to access this topic");
        }

        return userResource;
    }
}
