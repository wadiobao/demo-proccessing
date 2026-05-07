package com.example.demo.modules.identity.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.modules.identity.domain.model.NormalUser;
import com.example.demo.modules.identity.infrastructure.persistence.entity.NormalUserEntity;

@Repository
public interface SpringDataJpaNormalUserRepository extends JpaRepository<NormalUserEntity, Long> {
    Optional<NormalUser> findByUserName(String username);
}
