package com.example.demo.mongo.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.example.demo.mongo.entity.Content;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.search.FieldSearchPath;
import com.mongodb.client.model.search.SearchPath;
import com.mongodb.client.model.search.VectorSearchOptions;

public class ContentRepositoryCustomImpl implements ContentRepositoryCustom {
	@Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<Content> searchSimilar(List<Double> queryVector, int limit, String username) {
        String indexName = "vector_index_content"; // Tên index trên Atlas
        FieldSearchPath fieldSearchPath = SearchPath.fieldPath("embedding");

        // Sử dụng Aggregates.vectorSearch chuẩn của Driver mới
        List<Bson> pipeline = Arrays.asList(Aggregates.vectorSearch(
        		fieldSearchPath, // Trường chứa vector
                queryVector,
                indexName,
                limit,
                VectorSearchOptions.exactVectorSearchOptions().option("owner", username)
        ),
        		Aggregates.project(Projections.metaSearchScore("vectorSearchScore")));


        return mongoTemplate.getCollection("content")
                .aggregate(pipeline)
                .map(doc -> mongoTemplate.getConverter().read(Content.class, doc))
                .into(new ArrayList<>());
    }
    
    
}
