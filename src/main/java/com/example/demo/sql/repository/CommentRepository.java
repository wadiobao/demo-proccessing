package com.example.demo.sql.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.sql.entity.Comment;

import jakarta.transaction.Transactional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
	List<Comment> findByForm_FormId(String id);

	@Modifying
	@Transactional
	@Query("DELETE FROM Comment c WHERE c.form.id = ?1")
	void deleteAllByFormId(String formId);
}
