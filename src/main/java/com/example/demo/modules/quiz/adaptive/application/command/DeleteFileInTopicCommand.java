package com.example.demo.modules.quiz.adaptive.application.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.document.metadata.api.DocumentMetadataFacade;
import com.example.demo.modules.document.processing.api.DocumentProcessingFacade;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.UserResourceMongoEntity;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.repository.UserResourceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeleteFileInTopicCommand {

	private final DocumentProcessingFacade documentProcessingFacade;
    private final DocumentMetadataFacade documentMetadataFacade;
    private final UserResourceRepository userResourceRepository;
    
    @Transactional
    public StateResponse<Object> execute(String topicId, String fileId, String username) {
        log.info("Executing add file to topic command for user: {}, topic: {}, file id {}", username, topicId, fileId);

        UserResourceMongoEntity userResource = userResourceRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found"));

        if (!userResource.getUserName().equals(username)) {
            throw new SecurityException("You do not have permission to modify this topic");
        }
        
        if(!userResource.getContentIds().contains(fileId)) {
        	throw new SecurityException("There is no file has this id in this topic");
        }
        
        String deletedId = documentMetadataFacade.deleteByIdAndOwner(fileId, username);

        return StateResponse.builder()
                .message("Delete file in topic successfully")
                .result(deletedId)
                .build();
    }
}
