package com.example.demo.modules.community.forum.application.usecase.command;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.community.forum.api.dto.TopicRequest;
import com.example.demo.modules.community.forum.infrastructure.persistence.entity.Topic;
import com.example.demo.modules.community.forum.infrastructure.persistence.repository.TopicRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateTopicUseCase {

    private final TopicRepository topicRepository;

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public StateResponse<Object> execute(TopicRequest request) {
        Topic topic = Topic.builder().topic(request.getTopic()).build();
        topicRepository.save(topic);
        return StateResponse.builder().result(topic).build();
    }
}
