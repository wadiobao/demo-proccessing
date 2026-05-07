package com.example.demo.modules.identity.infrastructure.persistence.adapter;

import org.springframework.stereotype.Component;

import com.example.demo.modules.identity.domain.model.InvalidatedToken;
import com.example.demo.modules.identity.domain.repository.IInvalidatedTokenRepository;
import com.example.demo.modules.identity.infrastructure.persistence.mapper.IdentityEntityMapper;
import com.example.demo.modules.identity.infrastructure.persistence.repository.SpringDataJpaInvalidatedTokenRepository;

import lombok.RequiredArgsConstructor;

/**
 * Persistence adapter implementing the Domain IInvalidatedTokenRepository port.
 */
@Component
@RequiredArgsConstructor
public class InvalidatedTokenPersistenceAdapter implements IInvalidatedTokenRepository {

    private final SpringDataJpaInvalidatedTokenRepository jpaRepository;
    private final IdentityEntityMapper mapper;

    @Override
    public boolean existsById(String tokenId) {
        return jpaRepository.existsById(tokenId);
    }

    @Override
    public void save(InvalidatedToken token) {
        jpaRepository.save(mapper.toEntity(token));
    }
}
