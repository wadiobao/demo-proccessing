package com.example.demo.modules.quiz.adaptive.application.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.UserResourceMongoEntity;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.repository.UserResourceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Command for updating the metadata of a UserResource (Topic).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateTopicCommand {

    private final UserResourceRepository userResourceRepository;

    @Transactional
    public StateResponse<Object> execute(String id, String newTopicName, String username) {
        log.info("Updating topic name for resource {} to '{}' for user {}", id, newTopicName, username);

        UserResourceMongoEntity userResource = userResourceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found"));

        if (!userResource.getUserName().equals(username)) {
            throw new SecurityException("You do not have permission to modify this topic");
        }

        userResource.setTopic(newTopicName);
        userResourceRepository.save(userResource);

        return StateResponse.builder()
                .message("Topic updated successfully")
                .result(userResource)
                .build();
    }
}
