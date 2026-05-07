package com.example.demo.modules.quiz.adaptive.application.query;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.quiz.adaptive.api.response.TopicInfoResponse;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.UserResourceMongoEntity;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.repository.UserResourceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Query for retrieving summary information for all user learning topics.
 * Only reads system state.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GetTopicInfoQuery {

    private final UserResourceRepository userResourceRepository;

    public StateResponse<Object> execute(String username) {
        log.info("Executing get all topics info query for user: {}", username);
        List<UserResourceMongoEntity> userResources = userResourceRepository.findAllByUserName(username);

        List<TopicInfoResponse> topics = userResources.stream()
                .map(resource -> TopicInfoResponse.builder()
                        .id(resource.getId())
                        .topic(resource.getTopic())
                        .createdAt(resource.getCreatedAt())
                        .score(resource.getTheta())
                        .build())
                .collect(Collectors.toList());

        return StateResponse.builder()
                .message("Topics info retrieved successfully")
                .result(topics)
                .build();
    }
}
