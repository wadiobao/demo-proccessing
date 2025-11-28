package com.example.demo.sql.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.sql.entity.Comment;
import com.example.demo.sql.entity.Form;

@Repository
public interface FormRepository extends JpaRepository<Form, String>{
	Optional<Form> findById(String id);
	List<Form> findAllByOrderByNgayDangDesc();
	List<Form> findByTopic_TopicIdOrderByNgayDangDesc(Long topicId);
}
