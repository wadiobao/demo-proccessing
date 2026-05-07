package com.example.demo.modules.identity.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.modules.identity.infrastructure.persistence.entity.PermissionEntity;

@Repository
public interface SpringDataJpaPermissionRepository extends JpaRepository<PermissionEntity, String> {
}
