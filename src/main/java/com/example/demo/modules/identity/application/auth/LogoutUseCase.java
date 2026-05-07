package com.example.demo.modules.identity.application.auth;

import java.text.ParseException;
import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.modules.identity.domain.model.InvalidatedToken;
import com.example.demo.modules.identity.domain.repository.IInvalidatedTokenRepository;
import com.example.demo.modules.identity.infrastructure.security.JwtUtils;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogoutUseCase {

    private final IInvalidatedTokenRepository invalidatedTokenRepository;
    private final JwtUtils jwtUtils;

    @Transactional
    public void execute(HttpServletResponse response, String token) throws JOSEException, ParseException {
        jwtUtils.clearToken(response);

        SignedJWT signToken = jwtUtils.verifyToken(token);

        String jit = signToken.getJWTClaimsSet().getJWTID();
        Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(jit)
                .expiryTime(expiryTime)
                .build();

        invalidatedTokenRepository.save(invalidatedToken);
    }
}
