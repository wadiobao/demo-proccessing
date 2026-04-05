package com.example.demo.mongo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.mongo.entity.QuestionBank;

@Repository
public interface QuestionBankRepository extends MongoRepository<QuestionBank, String> {

    long countByContentId(String contentId);

    List<QuestionBank> findAllByContentId(String contentId);

    Optional<QuestionBank> findByContentIdAndQuestionHash(String contentId, String questionHash);

    @Query("{$text: {$search: ?0}}")
   Page<QuestionBank> searchByKeyword(String keyword,
            Pageable pageable);
}
