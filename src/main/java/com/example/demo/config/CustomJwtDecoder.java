package com.example.demo.config;

import java.util.Objects;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import com.example.demo.modules.identity.api.IdentityFacade;

@Component
public class CustomJwtDecoder implements JwtDecoder {

    @Value("${demo.secret.key}")
    private String SIGN_KEY;

    @Autowired
    private IdentityFacade identityFacade;

    private NimbusJwtDecoder decoder = null;

    @Override
    public Jwt decode(String token) throws JwtException {
        var response = identityFacade.introspect(token);
        if (!response.isValid()) {
            throw new JwtException("Invalid token");
        }

        if (Objects.isNull(decoder)) {
            SecretKeySpec secretKeySpec = new SecretKeySpec(SIGN_KEY.getBytes(), "HS512");
            decoder = NimbusJwtDecoder.withSecretKey(secretKeySpec).macAlgorithm(MacAlgorithm.HS512).build();
        }

        Jwt jwt = decoder.decode(token);
        if (jwt.getClaim("role") == null) {
            throw new JwtException("Invalid token type: Access token expected");
        }
        return jwt;
    }
}
