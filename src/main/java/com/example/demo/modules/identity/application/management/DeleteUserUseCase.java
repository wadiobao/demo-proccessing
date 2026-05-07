package com.example.demo.modules.identity.application.management;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.enums.ErrorCode;
import com.example.demo.exception.HandleException;
import com.example.demo.modules.identity.domain.repository.IUserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteUserUseCase {

    private final IUserRepository userRepository;

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void execute(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new HandleException(ErrorCode.USER_NOT_EXISTED);
        }
        userRepository.deleteById(userId);
    }
}
