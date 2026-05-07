package com.example.demo.modules.identity.infrastructure.persistence.adapter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.example.demo.modules.identity.domain.model.User;
import com.example.demo.modules.identity.domain.repository.IUserRepository;
import com.example.demo.modules.identity.infrastructure.persistence.mapper.IdentityEntityMapper;
import com.example.demo.modules.identity.infrastructure.persistence.repository.SpringDataJpaUserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Persistence adapter implementing the Domain IUserRepository port.
 * Translates between UserEntity (JPA) and User (Domain Model).
 */
@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements IUserRepository {

    private final SpringDataJpaUserRepository jpaRepository;
    private final IdentityEntityMapper mapper;

    @Override
    public Optional<User> findByUserName(String username) {
        return jpaRepository.findByUserName(username).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsByUserName(String username) {
        return jpaRepository.existsByUserName(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public User save(User user) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(user)));
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<User> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        // Map each UserEntity page slice to a User domain model page
        return jpaRepository.findAll(pageable).map(mapper::toDomain);
    }
}
