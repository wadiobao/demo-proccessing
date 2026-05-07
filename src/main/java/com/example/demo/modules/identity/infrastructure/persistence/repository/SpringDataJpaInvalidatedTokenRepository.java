package com.example.demo.modules.identity.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.modules.identity.infrastructure.persistence.entity.InvalidatedTokenEntity;

@Repository
public interface SpringDataJpaInvalidatedTokenRepository extends JpaRepository<InvalidatedTokenEntity, String> {
}
