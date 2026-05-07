package com.example.demo.modules.quiz.adaptive.application.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.UserResourceMongoEntity;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.repository.UserResourceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Command for deleting a UserResource (Topic).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeleteTopicCommand {

    private final UserResourceRepository userResourceRepository;

    @Transactional
    public StateResponse<Object> execute(String id, String username) {
        log.info("Deleting topic resource {} for user {}", id, username);

        UserResourceMongoEntity userResource = userResourceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found"));

        if (!userResource.getUserName().equals(username)) {
            throw new SecurityException("You do not have permission to delete this topic");
        }

        userResourceRepository.delete(userResource);

        return StateResponse.builder()
                .message("Topic deleted successfully")
                .build();
    }
}
