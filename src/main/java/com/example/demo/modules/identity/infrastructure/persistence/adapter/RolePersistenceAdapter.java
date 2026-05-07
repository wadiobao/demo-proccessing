package com.example.demo.modules.identity.infrastructure.persistence.adapter;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.example.demo.modules.identity.domain.model.Role;
import com.example.demo.modules.identity.domain.repository.IRoleRepository;
import com.example.demo.modules.identity.infrastructure.persistence.mapper.IdentityEntityMapper;
import com.example.demo.modules.identity.infrastructure.persistence.repository.SpringDataJpaRoleRepository;

import lombok.RequiredArgsConstructor;

/**
 * Persistence adapter implementing the Domain IRoleRepository port.
 */
@Component
@RequiredArgsConstructor
public class RolePersistenceAdapter implements IRoleRepository {

    private final SpringDataJpaRoleRepository jpaRepository;
    private final IdentityEntityMapper mapper;

    @Override
    public Optional<Role> findById(String name) {
        return jpaRepository.findById(name).map(mapper::toDomain);
    }

    @Override
    public Role save(Role role) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(role)));
    }

    @Override
    public boolean existsById(String name) {
        return jpaRepository.existsById(name);
    }
}
