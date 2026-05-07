package com.example.demo.modules.identity.domain.repository;

import java.util.List;
import java.util.Optional;

import com.example.demo.modules.identity.domain.model.Tier;

/**
 * Domain port for tier persistence operations.
 */
public interface ITierRepository {
    Optional<Tier> findById(String id);
    List<Tier> findAll();
}
