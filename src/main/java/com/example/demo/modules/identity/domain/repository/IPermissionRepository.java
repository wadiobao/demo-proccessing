package com.example.demo.modules.identity.domain.repository;

import java.util.List;
import java.util.Optional;

import com.example.demo.modules.identity.domain.model.Permission;

/**
 * Domain port for permission persistence operations.
 */
public interface IPermissionRepository {
    Optional<Permission> findByName(String name);
    List<Permission> findAll();
    Permission save(Permission permission);
    void deleteByName(String name);
    boolean existsByName(String name);
}
