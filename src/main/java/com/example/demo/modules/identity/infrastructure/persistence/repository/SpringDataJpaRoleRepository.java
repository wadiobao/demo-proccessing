package com.example.demo.modules.identity.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.modules.identity.infrastructure.persistence.entity.RoleEntity;

@Repository
public interface SpringDataJpaRoleRepository extends JpaRepository<RoleEntity, String> {
}
