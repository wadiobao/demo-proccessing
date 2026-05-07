package com.example.demo.modules.identity.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.demo.modules.identity.domain.model.User;

/**
 * Domain port for user persistence operations.
 * Implementations live in the infrastructure layer.
 */
public interface IUserRepository {
    Optional<User> findByUserName(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);
    boolean existsByUserName(String username);
    boolean existsByEmail(String email);
    boolean existsById(Long id);
    User save(User user);
    void deleteById(Long id);
    List<User> findAll();
    Page<User> findAll(Pageable pageable);
}
