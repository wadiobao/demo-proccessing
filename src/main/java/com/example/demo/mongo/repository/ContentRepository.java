package com.example.demo.mongo.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.mongo.entity.Content;

@Repository
public interface ContentRepository extends MongoRepository<Content, String>, ContentRepositoryCustom {
    Optional<Content> findFirstByContentAndOwner(String content, String owner);
}
