package com.example.demo.modules.quiz.adaptive.application.command;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.quiz.adaptive.application.service.AdaptiveQuizDocumentService;
import com.example.demo.modules.quiz.adaptive.application.service.AdaptiveQuizTopicService;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.UserResourceMongoEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Command for creating or syncing a topic resource with initial documents.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CreateTopicCommand {

    private final AdaptiveQuizDocumentService documentService;
    private final AdaptiveQuizTopicService topicService;

    @Transactional
    public StateResponse<Object> execute(String topic, List<MultipartFile> files, int sessionSize, String username) throws Exception {
        log.info("Creating topic '{}' for user '{}' with {} files", topic, username, files != null ? files.size() : 0);

        List<String> metadataIds = new ArrayList<>();
        // 1. Process files and get metadata IDs
        if (files != null && !files.isEmpty()) {
            metadataIds = documentService.extractMetadataIds(files, username);
        }
        // 2. Sync topic resource (create if not exists, add files, set session size)
        UserResourceMongoEntity userResource = topicService.syncTopicResource(username, topic, metadataIds, sessionSize);

        return StateResponse.builder()
                .message("Topic created/updated successfully")
                .result(userResource)
                .build();
    }
}
