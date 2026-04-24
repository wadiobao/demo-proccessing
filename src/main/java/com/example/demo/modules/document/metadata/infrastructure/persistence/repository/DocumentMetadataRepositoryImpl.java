package com.example.demo.modules.document.metadata.infrastructure.persistence.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import com.example.demo.modules.document.metadata.infrastructure.persistence.entity.DocumentMetadataMongoEntity;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.search.FieldSearchPath;
import com.mongodb.client.model.search.SearchPath;
import com.mongodb.client.model.search.VectorSearchOptions;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class DocumentMetadataRepositoryImpl implements DocumentMetadataRepositoryCustom {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Value("${mongodb.atlas.vector-index:vector_index_content}")
    private String indexName;

    @Override
    public List<DocumentMetadataMongoEntity> searchSimilar(List<Double> queryVector, int limit, String username) {
        try {
            FieldSearchPath fieldSearchPath = SearchPath.fieldPath("embedding");

            List<Bson> pipeline = Arrays.asList(Aggregates.vectorSearch(
                    fieldSearchPath,
                    queryVector,
                    indexName,
                    limit,
                    VectorSearchOptions.exactVectorSearchOptions().option("owner", username)),
                    Aggregates.project(Projections.metaSearchScore("vectorSearchScore")));

            log.info("Executing vector search for metadata on index: {}", indexName);

            return mongoTemplate.getCollection("content")
                    .aggregate(pipeline)
                    .map(doc -> mongoTemplate.getConverter().read(DocumentMetadataMongoEntity.class, doc))
                    .into(new ArrayList<>());
        } catch (Exception e) {
            log.error("Vector search for metadata failed: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}
