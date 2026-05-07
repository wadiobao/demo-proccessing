package com.example.demo.modules.identity.domain.repository;

import com.example.demo.modules.identity.domain.model.InvalidatedToken;

/**
 * Domain port for invalidated token persistence operations.
 */
public interface IInvalidatedTokenRepository {
    boolean existsById(String tokenId);
    void save(InvalidatedToken token);
}
