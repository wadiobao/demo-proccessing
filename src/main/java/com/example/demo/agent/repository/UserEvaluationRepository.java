package com.example.demo.agent.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.agent.entity.UserEvaluation;

@Repository
public interface UserEvaluationRepository extends MongoRepository<UserEvaluation, String> {
	Optional<UserEvaluation> findByEmail(String email);
	List<UserEvaluation> findAllByEmail(String email);
	
	@Query("""
	        SELECT a FROM Article a
	        WHERE a.email = :email
	        AND (
	            """ +
	            " :#{#tags == null ? false : '1'} = '1' " + // chỉ để tránh lỗi nếu tags null
	            " OR " +
	            " EXISTS (SELECT 1 FROM Article a2 WHERE a2.id = a.id AND (" +
	            " " + 
	            " :tags IS NULL OR " +
	            " ( " +
	            "   LOWER(a.conceptTags) LIKE CONCAT('%', LOWER(:tags[0]), '%') " +
	            " )" +
	            "))"
	    )
	    List<UserEvaluation> findByEmailAndTagsLike(@Param("email") String email, @Param("tags") List<String> tags);

}