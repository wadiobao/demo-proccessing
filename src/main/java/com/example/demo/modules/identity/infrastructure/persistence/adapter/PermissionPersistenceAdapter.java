package com.example.demo.modules.identity.infrastructure.persistence.adapter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.example.demo.modules.identity.domain.model.Permission;
import com.example.demo.modules.identity.domain.repository.IPermissionRepository;
import com.example.demo.modules.identity.infrastructure.persistence.mapper.IdentityEntityMapper;
import com.example.demo.modules.identity.infrastructure.persistence.repository.SpringDataJpaPermissionRepository;

import lombok.RequiredArgsConstructor;

/**
 * Persistence adapter for Permission entities.
 */
@Component
@RequiredArgsConstructor
public class PermissionPersistenceAdapter implements IPermissionRepository {

    private final SpringDataJpaPermissionRepository jpaRepository;
    private final IdentityEntityMapper mapper;

    @Override
    public Optional<Permission> findByName(String name) {
        return jpaRepository.findById(name).map(mapper::toDomain);
    }

    @Override
    public List<Permission> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Permission save(Permission permission) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(permission)));
    }

    @Override
    public void deleteByName(String name) {
        jpaRepository.deleteById(name);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsById(name);
    }
}
