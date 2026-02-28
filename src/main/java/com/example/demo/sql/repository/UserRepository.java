package com.example.demo.sql.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.sql.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	boolean existsByUserName(String username);

	boolean existsByEmail(String email);

	Optional<User> findByUserName(String username);

	Optional<User> findByEmail(String email);
}
