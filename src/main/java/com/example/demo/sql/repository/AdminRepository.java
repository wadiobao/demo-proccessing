package com.example.demo.sql.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.demo.sql.entity.Admin;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
}
