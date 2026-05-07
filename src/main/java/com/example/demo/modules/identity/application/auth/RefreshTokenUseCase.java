package com.example.demo.modules.identity.application.auth;

import java.text.ParseException;
import java.util.Date;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.enums.ErrorCode;
import com.example.demo.exception.HandleException;
import com.example.demo.modules.identity.domain.model.InvalidatedToken;
import com.example.demo.modules.identity.domain.model.User;
import com.example.demo.modules.identity.domain.repository.IInvalidatedTokenRepository;
import com.example.demo.modules.identity.domain.repository.IUserRepository;
import com.example.demo.modules.identity.infrastructure.security.JwtUtils;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenUseCase {

    private final IUserRepository userRepository;
    private final IInvalidatedTokenRepository invalidatedTokenRepository;
    private final JwtUtils jwtUtils;

    @Transactional
    public RefreshResult execute(String token) throws JOSEException, ParseException {
        SignedJWT jwtSignedJWT = jwtUtils.verifyToken(token);

        String jit = jwtSignedJWT.getJWTClaimsSet().getJWTID();
        Date expiryTime = jwtSignedJWT.getJWTClaimsSet().getExpirationTime();

        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(jit)
                .expiryTime(expiryTime)
                .build();

        invalidatedTokenRepository.save(invalidatedToken);

        String username = jwtSignedJWT.getJWTClaimsSet().getSubject();
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new HandleException(ErrorCode.USER_NOT_EXISTED));

        String accessToken = jwtUtils.generateToken(user, false);

        return RefreshResult.builder()
                .accessTokenCookie(jwtUtils.generateAccessCookie(accessToken))
                .refreshTokenCookie(jwtUtils.generateRefreshCookie(token))
                .authenticated(true)
                .build();
    }

    @Data
    @Builder
    public static class RefreshResult {
        private ResponseCookie accessTokenCookie;
        private ResponseCookie refreshTokenCookie;
        private boolean authenticated;
    }
}
