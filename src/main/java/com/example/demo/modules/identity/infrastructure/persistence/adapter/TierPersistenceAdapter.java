package com.example.demo.modules.identity.infrastructure.persistence.adapter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.example.demo.modules.identity.domain.model.Tier;
import com.example.demo.modules.identity.domain.repository.ITierRepository;
import com.example.demo.modules.identity.infrastructure.persistence.mapper.IdentityEntityMapper;
import com.example.demo.modules.identity.infrastructure.persistence.repository.SpringDataJpaTierRepository;

import lombok.RequiredArgsConstructor;

/**
 * Persistence adapter implementing the Domain ITierRepository port.
 */
@Component
@RequiredArgsConstructor
public class TierPersistenceAdapter implements ITierRepository {

    private final SpringDataJpaTierRepository jpaRepository;
    private final IdentityEntityMapper mapper;

    @Override
    public Optional<Tier> findById(String id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Tier> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
