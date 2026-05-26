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
    private final com.example.demo.modules.quiz.evaluation.application.service.IRTCalculator irtCalculator;

    public StateResponse<Object> execute(String username) {
        log.info("Executing get all topics info query for user: {}", username);
        List<UserResourceMongoEntity> userResources = userResourceRepository.findAllByUserName(username);

        List<TopicInfoResponse> topics = userResources.stream()
                .map(resource -> {
                        int elo = resource.getElo();
                        // fall back to theta-derived ELO for legacy docs where elo was 0
                        if (elo == 0) {
                            elo = irtCalculator.thetaToElo(resource.getTheta());
                        }
                        int masteryLevel = resource.getMastery() == 0
                                ? irtCalculator.calculateMasteryLevel(resource.getTheta())
                                : resource.getMastery();
                        return TopicInfoResponse.builder()
                                .id(resource.getId())
                                .topic(resource.getTopic())
                                .createdAt(resource.getCreatedAt())
                                .elo(elo)
                                .mastery(irtCalculator.getMasteryLabel(masteryLevel))
                                .build();
                })
                .collect(Collectors.toList());

        return StateResponse.builder()
                .message("Topics info retrieved successfully")
                .result(topics)
                .build();
    }
}
