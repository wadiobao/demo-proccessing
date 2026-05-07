package com.example.demo.modules.document.shared.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.demo.modules.document.shared.domain.model.Major;

@Repository
public interface MajorRepository extends JpaRepository<Major, Long> {
    Optional<Major> findByCode(String code);
}
