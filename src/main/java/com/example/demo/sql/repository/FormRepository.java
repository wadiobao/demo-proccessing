package com.example.demo.sql.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.sql.entity.Form;

@Repository
public interface FormRepository extends JpaRepository<Form, String> {
	Optional<Form> findById(String id);

	Page<Form> findAllByOrderByNgayDangDesc(Pageable pageable);

	Page<Form> findByTopic_TopicIdOrderByNgayDangDesc(Long topicId, Pageable pageable);

	@org.springframework.data.jpa.repository.Query(value = "SELECT * FROM form_data f " +
			"JOIN form_content c ON f.form_id = c.id " +
			"WHERE MATCH(f.tieu_de) AGAINST(?1 IN NATURAL LANGUAGE MODE) " +
			"OR MATCH(c.noi_dung) AGAINST(?1 IN NATURAL LANGUAGE MODE)", nativeQuery = true)
	Page<Form> searchByKeyword(String keyword, Pageable pageable);
}
