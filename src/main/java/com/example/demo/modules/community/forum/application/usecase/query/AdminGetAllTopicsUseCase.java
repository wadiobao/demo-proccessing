package com.example.demo.modules.community.forum.application.usecase.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.community.forum.api.dto.TopicResponse;
import com.example.demo.modules.community.forum.infrastructure.persistence.entity.Topic;
import com.example.demo.modules.community.forum.infrastructure.persistence.repository.TopicRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminGetAllTopicsUseCase {

    private final TopicRepository topicRepository;

    /**
     * Retrieve all topics ordered by TopicId in ascending order for administrative control.
     * Maps Topic entities to TopicResponse DTOs to prevent Hibernate lazy serialization issues.
     * 
     * @param pageable pagination and sorting details
     * @return StateResponse containing the paginated list of TopicResponse DTOs
     */
    public StateResponse<Object> execute(Pageable pageable) {
        Page<Topic> topics = topicRepository.findAllByOrderByTopicIdAsc(pageable);
        Page<TopicResponse> responses = topics.map(topic -> TopicResponse.builder()
                .topicId(topic.getTopicId())
                .topic(topic.getTopic())
                .build());
        return StateResponse.builder().result(responses).build();
    }
}

