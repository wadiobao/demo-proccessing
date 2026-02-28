package com.example.demo.sql.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.enums.Major;
import com.example.demo.sql.entity.PdfFile;

@Repository
public interface PdfFileRepository extends JpaRepository<PdfFile, Long> {
	boolean existsByCloudinaryId(String cloudinaryId);

	Optional<PdfFile> findByCloudinaryId(String publicId);

	List<PdfFile> findAllByCloudinaryIdIn(List<String> publicIds);

	Page<PdfFile> findAllByMajor(Major major, Pageable pageable);
}
