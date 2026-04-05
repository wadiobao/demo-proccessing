package com.example.demo.sql.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.demo.sql.entity.Major;

@Repository
public interface MajorRepository extends JpaRepository<Major, Long> {
    Optional<Major> findByCode(String code);
    boolean existsByCode(String code);
}
