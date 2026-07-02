package com.example.demo.modules.community.forum.application.usecase.command;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.community.forum.api.dto.TopicRequest;
import com.example.demo.modules.community.forum.api.dto.TopicResponse;
import com.example.demo.modules.community.forum.infrastructure.persistence.entity.Topic;
import com.example.demo.modules.community.forum.infrastructure.persistence.repository.TopicRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminUpdateTopicUseCase {

    private final TopicRepository topicRepository;

    /**
     * Update the name of a Topic by its ID.
     * Only users with ADMIN role are authorized.
     * Maps the updated Topic entity to a TopicResponse DTO to avoid serialization issues.
     * 
     * @param topicId ID of the topic to be updated
     * @param request payload containing the new topic name
     * @return StateResponse containing the updated TopicResponse
     * @throws RuntimeException if the topic is not found
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public StateResponse<Object> execute(Long topicId, TopicRequest request) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));
        
        topic.setTopic(request.getTopic());
        topicRepository.save(topic);
        
        TopicResponse response = TopicResponse.builder()
                .topicId(topic.getTopicId())
                .topic(topic.getTopic())
                .build();
        
        return StateResponse.builder().result(response).build();
    }
}

