package com.example.demo.modules.quiz.adaptive.application.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.document.metadata.api.DocumentMetadataFacade;
import com.example.demo.modules.document.metadata.domain.model.DocumentMetadata;
import com.example.demo.modules.document.processing.api.DocumentProcessingFacade;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.UserResourceMongoEntity;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.repository.UserResourceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Command for adding and associating documents with a learning topic.
 * Changes system state by updating topic document links.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AddFileToTopicCommand {

    private final DocumentProcessingFacade documentProcessingFacade;
    private final DocumentMetadataFacade documentMetadataFacade;
    private final UserResourceRepository userResourceRepository;

    @Transactional
    public StateResponse<Object> execute(MultipartFile file, String topicId, String username) throws Exception {
        log.info("Executing add file to topic command for user: {}, topic: {}", username, topicId);

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        UserResourceMongoEntity userResource = userResourceRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found"));

        if (!userResource.getUserName().equals(username)) {
            throw new SecurityException("You do not have permission to modify this topic");
        }

        String rawText = documentProcessingFacade.processDocument(file).getRawText();
        DocumentMetadata metadata = documentMetadataFacade.findOrCreateMetadata(rawText, username, file.getOriginalFilename());

        if (!userResource.getContentIds().contains(metadata.getId())) {
            userResource.getContentIds().add(metadata.getId());
            userResourceRepository.save(userResource);
            log.info("File {} added successfully to topic {}", file.getOriginalFilename(), topicId);
        }

        return StateResponse.builder()
                .message("File added to topic successfully")
                .result(metadata.getId())
                .build();
    }
}
