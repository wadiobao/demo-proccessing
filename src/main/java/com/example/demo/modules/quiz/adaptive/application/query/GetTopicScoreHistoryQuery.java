package com.example.demo.modules.quiz.adaptive.application.query;

import org.springframework.stereotype.Service;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.UserResourceMongoEntity;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.repository.UserResourceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Query for retrieving historical mastery scores for a specific learning topic.
 * Only reads system state.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GetTopicScoreHistoryQuery {

    private final UserResourceRepository userResourceRepository;

    public StateResponse<Object> execute(String topicId, String username) {
        log.info("Executing get topic score history query for user: {}, topic: {}", username, topicId);
        UserResourceMongoEntity userResource = userResourceRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found"));

        if (!userResource.getUserName().equals(username)) {
            throw new SecurityException("You do not have permission to view this topic");
        }

        return StateResponse.builder()
                .message("Topic score history retrieved successfully")
                .result(userResource.getThetaHistory())
                .build();
    }
}
