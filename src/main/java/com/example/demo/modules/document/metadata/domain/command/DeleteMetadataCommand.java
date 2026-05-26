package com.example.demo.modules.document.metadata.domain.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.modules.document.metadata.infrastructure.persistence.entity.DocumentMetadataMongoEntity;
import com.example.demo.modules.document.metadata.infrastructure.persistence.repository.DocumentMetadataRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class DeleteMetadataCommand {
	
	DocumentMetadataRepository documentMetadataRepository;
	
	@Transactional
    public String execute(String fileId, String username) {
        log.info("Executing add file to topic command for user: {}, file id {}", username, fileId);

        DocumentMetadataMongoEntity doc =  documentMetadataRepository.findByIdAndOwner(fileId, username).orElseThrow(
        		() -> new SecurityException("You do not have permission to delete this file"));
        
        documentMetadataRepository.delete(doc);
        return doc.getId();

    }

}
