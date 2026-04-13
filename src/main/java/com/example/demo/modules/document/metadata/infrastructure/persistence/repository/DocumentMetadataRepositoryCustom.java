package com.example.demo.modules.document.metadata.infrastructure.persistence.repository;

import java.util.List;
import com.example.demo.modules.document.metadata.infrastructure.persistence.entity.DocumentMetadataMongoEntity;

public interface DocumentMetadataRepositoryCustom {
    List<DocumentMetadataMongoEntity> searchSimilar(List<Double> queryVector, int limit, String username);
}
