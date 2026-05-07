package com.example.demo.modules.identity.domain.repository;

import java.util.Optional;

import com.example.demo.modules.identity.domain.model.Role;

/**
 * Domain port for role persistence operations.
 */
public interface IRoleRepository {
    Optional<Role> findById(String name);
    Role save(Role role);
    boolean existsById(String name);
}
