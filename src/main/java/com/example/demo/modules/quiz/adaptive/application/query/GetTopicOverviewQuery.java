package com.example.demo.modules.quiz.adaptive.application.query;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.dto.StateResponse;
import com.example.demo.enums.ErrorCode;
import com.example.demo.exception.HandleException;
import com.example.demo.modules.document.metadata.api.DocumentMetadataFacade;
import com.example.demo.modules.quiz.adaptive.api.response.TopicFileResponse;
import com.example.demo.modules.quiz.adaptive.api.response.TopicOverviewResponse;
import com.example.demo.modules.quiz.evaluation.application.service.IRTCalculator;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.UserResourceMongoEntity;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.repository.UserResourceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Query that merges topic metadata, IRT/ELO stats, theta history, and file list
 * into a single response for the {@code GET /topics/overview} endpoint.
 *
 * <p>Query complexity: O(2) — one {@code findById} on {@code user_resource} and one
 * batch {@code findByIds} on {@code document_metadata}. No N+1 pattern.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GetTopicOverviewQuery {

    private final UserResourceRepository userResourceRepository;
    private final DocumentMetadataFacade documentMetadataFacade;
    private final IRTCalculator irtCalculator;

    /**
     * Retrieves the unified topic overview for the given topic ID and user.
     *
     * @param topicId  the unique identifier of the topic
     * @param username the authenticated user's username
     * @return a {@link StateResponse} wrapping {@link TopicOverviewResponse}
     */
    public StateResponse<Object> execute(String topicId, String username) {
        log.info("Executing topic overview query for user: {}, topic: {}", username, topicId);

        // Step 1: single findById — no N+1 risk
        UserResourceMongoEntity resource = userResourceRepository.findById(topicId)
                .orElseThrow(() -> new HandleException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!resource.getUserName().equals(username)) {
            throw new SecurityException("Access denied for topic: " + topicId);
        }

        // Step 2: resolve mastery label (handle legacy docs where mastery == 0)
        int mastery = resource.getMastery() == 0
                ? irtCalculator.calculateMasteryLevel(resource.getTheta())
                : resource.getMastery();
        String masteryLabel = irtCalculator.getMasteryLabel(mastery);

        // Step 3: ELO fields — Calculate on-the-fly to support legacy data where elo was 0
        int elo = resource.getElo();
        int highestElo = resource.getHighestElo();
        if (elo == 0) {
            elo = irtCalculator.thetaToElo(resource.getTheta());
        }
        
        if(highestElo == 0) {
        	highestElo = elo;
        }
        int eloToNext = irtCalculator.eloToNextLevel(resource.getTheta());

        // Step 4: batch fetch files in a single query — prevents N+1
        List<String> contentIds = resource.getContentIds();
        List<TopicFileResponse> files = contentIds == null || contentIds.isEmpty()
                ? Collections.emptyList()
                : documentMetadataFacade.findByIds(contentIds)
                        .stream()
                        .map(doc -> TopicFileResponse.builder()
                                .id(doc.getId())
                                .originalName(doc.getOriginalName())
                                .content(doc.getContent())
                                .build())
                        .collect(Collectors.toList());

        TopicOverviewResponse overview = TopicOverviewResponse.builder()
                .id(resource.getId())
                .topic(resource.getTopic())
                .createdAt(resource.getCreatedAt())
                .sessionSize(resource.getSessionSize())
                .theta(resource.getTheta())
                .mastery(mastery)
                .masteryLabel(masteryLabel)
                .elo(elo)
                .eloToNextLevel(eloToNext)
                .highestElo(highestElo)
                .thetaHistory(resource.getThetaHistory())
                .files(files)
                .build();

        return StateResponse.builder()
                .message("Topic overview retrieved successfully")
                .result(overview)
                .build();
    }
}
