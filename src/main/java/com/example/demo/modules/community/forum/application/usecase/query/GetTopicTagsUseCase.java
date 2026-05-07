package com.example.demo.modules.community.forum.application.usecase.query;

import org.springframework.stereotype.Service;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.community.forum.infrastructure.persistence.repository.FormRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetTopicTagsUseCase {

    private final FormRepository formRepository;

    public StateResponse<Object> execute(Long topicId) {
        return StateResponse.builder().result(formRepository.findTagsByTopicId(topicId)).build();
    }
}
