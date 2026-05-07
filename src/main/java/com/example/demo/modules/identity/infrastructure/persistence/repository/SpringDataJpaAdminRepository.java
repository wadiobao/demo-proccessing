package com.example.demo.modules.identity.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.modules.identity.domain.model.Admin;
import com.example.demo.modules.identity.infrastructure.persistence.entity.AdminEntity;

@Repository
public interface SpringDataJpaAdminRepository extends JpaRepository<AdminEntity, Long> {
    Optional<Admin> findByUserName(String username);
}
