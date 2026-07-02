package com.example.demo.modules.identity.application.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.enums.ErrorCode;
import com.example.demo.exception.HandleException;
import com.example.demo.modules.identity.api.dto.LoginRequest;
import com.example.demo.modules.identity.api.dto.LoginResult;
import com.example.demo.modules.identity.domain.model.NormalUser;
import com.example.demo.modules.identity.domain.model.User;
import com.example.demo.modules.identity.domain.repository.IUserRepository;
import com.example.demo.modules.identity.infrastructure.security.JwtUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserLoginUseCase {

    private final IUserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public LoginResult execute(LoginRequest request) {
        String loginIdentifier = request.getUserName();
        User user = userRepository.findByUserName(loginIdentifier)
                .or(() -> userRepository.findByEmail(loginIdentifier))
                .orElseThrow(() -> new HandleException(ErrorCode.BAD_CREDENTIALS));

        if (!(user instanceof NormalUser)) {
            throw new HandleException(ErrorCode.UNAUTHORIZED);
        }

        NormalUser normalUser = (NormalUser) user;

        // BẢO MẬT/LOGIC: Nếu tài khoản chỉ liên kết qua Google và chưa được thiết lập mật khẩu cục bộ
        if (normalUser.getProvider() == com.example.demo.modules.identity.domain.model.AuthProvider.GOOGLE && normalUser.getPassword() == null) {
            throw new HandleException(ErrorCode.BAD_CREDENTIALS);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new HandleException(ErrorCode.BAD_CREDENTIALS);
        }

        String accessToken = jwtUtils.generateToken(user, false);
        String refreshToken = jwtUtils.generateToken(user, true);

        return LoginResult.builder()
                .accessTokenCookie(jwtUtils.generateAccessCookie(accessToken))
                .refreshTokenCookie(jwtUtils.generateRefreshCookie(refreshToken))
                .authenticated(true)
                .build();
    }

}
