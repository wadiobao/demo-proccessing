package com.example.demo.modules.identity.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.modules.identity.infrastructure.persistence.entity.UserEntity;

@Repository
public interface SpringDataJpaUserRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByUserName(String username);
    boolean existsByEmail(String email);
    Optional<UserEntity> findByUserName(String username);
    Optional<UserEntity> findByEmail(String email);
}
