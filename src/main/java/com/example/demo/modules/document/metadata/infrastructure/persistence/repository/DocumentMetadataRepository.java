package com.example.demo.modules.document.metadata.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.modules.document.metadata.infrastructure.persistence.entity.DocumentMetadataMongoEntity;

@Repository
public interface DocumentMetadataRepository extends MongoRepository<DocumentMetadataMongoEntity, String>, DocumentMetadataRepositoryCustom {
    Optional<DocumentMetadataMongoEntity> findFirstByContentAndOwner(String content, String owner);
}
