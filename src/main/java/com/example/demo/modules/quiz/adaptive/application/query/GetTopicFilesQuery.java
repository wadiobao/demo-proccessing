package com.example.demo.modules.quiz.adaptive.application.query;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.document.metadata.api.DocumentMetadataFacade;
import com.example.demo.modules.quiz.adaptive.api.response.TopicFileResponse;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.UserResourceMongoEntity;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.repository.UserResourceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Query for retrieving files associated with a learning topic.
 * Only reads system state.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GetTopicFilesQuery {

    private final DocumentMetadataFacade documentMetadataFacade;
    private final UserResourceRepository userResourceRepository;

    public StateResponse<Object> execute(String topicId, String username) {
        log.info("Executing get topic files query for user: {}, topic: {}", username, topicId);
        UserResourceMongoEntity userResource = userResourceRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found"));

        if (!userResource.getUserName().equals(username)) {
            throw new SecurityException("You do not have permission to view this topic");
        }

        List<TopicFileResponse> files = documentMetadataFacade.findByIds(userResource.getContentIds())
                .stream()
                .map(t -> TopicFileResponse.builder()
                        .id(t.getId())
                        .content(t.getContent())
                        .originalName(t.getOriginalName())
                        .build())
                .collect(Collectors.toList());

        return StateResponse.builder()
                .message("Topic files retrieved successfully")
                .result(files)
                .build();
    }
}
