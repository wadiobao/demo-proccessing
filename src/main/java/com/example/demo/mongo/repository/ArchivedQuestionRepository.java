package com.example.demo.mongo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.mongo.entity.ArchivedQuestion;

@Repository
public interface ArchivedQuestionRepository extends MongoRepository<ArchivedQuestion, String> {
	List<ArchivedQuestion> findAllByAuthorOrderByCreatedAtDesc(String author);
	Optional<ArchivedQuestion> findFirstByAuthorOrderByCreatedAtAsc(String author);
	ArchivedQuestion findByAuthorAndTitle(String author, String title);
	long countByAuthor(String author);
}
