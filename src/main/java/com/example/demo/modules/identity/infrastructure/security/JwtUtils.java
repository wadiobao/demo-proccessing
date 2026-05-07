package com.example.demo.modules.identity.infrastructure.security;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import com.example.demo.enums.ErrorCode;
import com.example.demo.exception.HandleException;
import com.example.demo.modules.identity.domain.model.Role;
import com.example.demo.modules.identity.domain.model.User;
import com.example.demo.modules.identity.domain.repository.IInvalidatedTokenRepository;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.NonFinal;

/**
 * Core utility for JSON Web Token (JWT) lifecycle management.
 */
@Component
public class JwtUtils {

    @Autowired
    IInvalidatedTokenRepository invalidatedTokenRepository;

    @NonFinal
    @Value("${demo.secret.key}")
    String SIGN_KEY;

    @NonFinal
    @Value("${demo.time.token.refresh}")
    int REFRESH_TiME;

    @NonFinal
    @Value("${demo.time.token.access}")
    int ACCESS_TiME;

    public String generateToken(User user, boolean isRefresh) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet claimsSet;

        if (isRefresh) {
            claimsSet = new JWTClaimsSet.Builder().jwtID(UUID.randomUUID().toString()).subject(user.getUserName())
                    .issueTime(new Date())
                    .expirationTime(new Date(Instant.now().plus(REFRESH_TiME, ChronoUnit.SECONDS).toEpochMilli()))
                    .claim("uid", user.getId())
                    .build();
        } else {
            claimsSet = new JWTClaimsSet.Builder().jwtID(UUID.randomUUID().toString()).subject(user.getUserName())
                    .issuer("freequizai.com").issueTime(new Date())
                    .expirationTime(new Date(Instant.now().plus(ACCESS_TiME, ChronoUnit.SECONDS).toEpochMilli()))
                    .claim("role", user.getRole().getName())
                    .claim("uid", user.getId())
                    .claim("scope", buildScope(user)).build();
        }

        Payload payload = new Payload(claimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGN_KEY.getBytes()));
        } catch (KeyLengthException e) {
            e.printStackTrace();
        } catch (JOSEException e) {
            e.printStackTrace();
        }
        return jwsObject.serialize();
    }

    public String buildScope(User user) {
        StringJoiner joiner = new StringJoiner(" ");
        Role r = user.getRole();
        if (!CollectionUtils.isEmpty(r.getPermissions())) {
            r.getPermissions().forEach(p -> joiner.add(p.getName()));
        }
        return joiner.toString();
    }

    public SignedJWT verifyToken(String token) throws JOSEException, ParseException {
        try {
            JWSVerifier jwsVerifier = new MACVerifier(SIGN_KEY.getBytes());
            SignedJWT jwtSignedJWT = SignedJWT.parse(token);
            Date expityTime = jwtSignedJWT.getJWTClaimsSet().getExpirationTime();
            boolean verified = jwtSignedJWT.verify(jwsVerifier);

            if (!(verified && expityTime.after(new Date()))) {
                throw new HandleException(ErrorCode.UNAUTHENTICATED);
            }

            if (invalidatedTokenRepository.existsById(jwtSignedJWT.getJWTClaimsSet().getJWTID())) {
                throw new HandleException(ErrorCode.UNAUTHENTICATED);
            }

            return jwtSignedJWT;
        } catch (Exception e) {
            throw new HandleException(ErrorCode.UNAUTHENTICATED);
        }
    }

    public ResponseCookie generateAccessCookie(String token) {
        return ResponseCookie.from("access-token", token).httpOnly(true).path("/").maxAge(ACCESS_TiME)
                .sameSite("Lax").build();
    }

    public ResponseCookie generateRefreshCookie(String token) {
        return ResponseCookie.from("refresh-token", token).httpOnly(true).path("/").maxAge(REFRESH_TiME)
                .sameSite("Lax").build();
    }

    public void clearToken(HttpServletResponse response) {
        ResponseCookie accessTokenCookie = ResponseCookie.from("access-token", "").httpOnly(true).path("/").maxAge(0)
                .build();
        response.addHeader("Set-Cookie", accessTokenCookie.toString());

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh-token", "").httpOnly(true).path("/").maxAge(0)
                .build();
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());
    }
}
