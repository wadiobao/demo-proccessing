package com.example.demo.sql.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.sql.entity.Form;

@Repository
public interface FormRepository extends JpaRepository<Form, String> {
	@Override
	Optional<Form> findById(String id);

	Page<Form> findAllByOrderByNgayDangDesc(Pageable pageable);

	Page<Form> findByTopic_TopicIdOrderByNgayDangDesc(Long topicId, Pageable pageable);

	@Query("SELECT f FROM Form f WHERE f.tieuDe LIKE %:keyword% OR f.content.noiDung LIKE %:keyword%")
	Page<Form> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
