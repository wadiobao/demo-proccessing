package com.example.demo.sql.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.demo.sql.entity.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {
}
