package com.example.demo.modules.identity.application.auth;

import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.enums.ErrorCode;
import com.example.demo.exception.HandleException;
import com.example.demo.modules.identity.api.dto.LoginRequest;
import com.example.demo.modules.identity.domain.model.Admin;
import com.example.demo.modules.identity.domain.model.User;
import com.example.demo.modules.identity.domain.repository.IUserRepository;
import com.example.demo.modules.identity.infrastructure.security.JwtUtils;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminLoginUseCase {

    private final IUserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public LoginResult execute(LoginRequest request) {
        User user = userRepository.findByUserName(request.getUserName())
                .orElseThrow(() -> new HandleException(ErrorCode.USER_NOT_EXISTED));

        if (!(user instanceof Admin)) {
            throw new HandleException(ErrorCode.UNAUTHORIZED);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new HandleException(ErrorCode.UNAUTHENTICATED);
        }

        String accessToken = jwtUtils.generateToken(user, false);
        String refreshToken = jwtUtils.generateToken(user, true);

        return LoginResult.builder()
                .accessTokenCookie(jwtUtils.generateAccessCookie(accessToken))
                .refreshTokenCookie(jwtUtils.generateRefreshCookie(refreshToken))
                .authenticated(true)
                .build();
    }

    @Data
    @Builder
    public static class LoginResult {
        private ResponseCookie accessTokenCookie;
        private ResponseCookie refreshTokenCookie;
        private boolean authenticated;
    }
}
