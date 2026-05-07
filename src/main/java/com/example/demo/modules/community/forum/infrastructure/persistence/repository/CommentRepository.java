package com.example.demo.modules.community.forum.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.modules.community.forum.infrastructure.persistence.entity.Comment;

import jakarta.transaction.Transactional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
	Page<Comment> findByForm_FormId(String id, Pageable pageable);

	@Modifying
	@Transactional
	@Query("DELETE FROM Comment c WHERE c.form.id = ?1")
	void deleteAllByFormId(String formId);
}
