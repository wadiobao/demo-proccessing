package com.example.demo.modules.identity.application.user;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.enums.ErrorCode;
import com.example.demo.exception.HandleException;
import com.example.demo.modules.identity.api.dto.UserProfileResponse;
import com.example.demo.modules.identity.application.mapper.UserMapper;
import com.example.demo.modules.identity.domain.model.User;
import com.example.demo.modules.identity.domain.repository.IUserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetMyProfileUseCase {

    private final IUserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserProfileResponse execute() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        User user = userRepository.findByUserName(name)
                .orElseThrow(() -> new HandleException(ErrorCode.USER_NOT_EXISTED));

        return userMapper.toResponse(user);
    }
}
