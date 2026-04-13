package com.example.demo.config;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CookieBearerTokenResolver implements BearerTokenResolver {

    private final JwtCookieResolver jwtCookieResolver;

    public CookieBearerTokenResolver(JwtCookieResolver jwtCookieResolver) {
        this.jwtCookieResolver = jwtCookieResolver;
    }

    @Override
    public String resolve(HttpServletRequest request) {
        // Log all cookies for debugging
        if (request.getCookies() != null) {
            String cookieNames = Arrays.stream(request.getCookies())
                    .map(Cookie::getName)
                    .collect(Collectors.joining(", "));
            log.info("Cookies found in request: [{}]", cookieNames);
        } else {
            log.info("No cookies found in request");
        }

        // Try to get token from cookie first
        String tokenFromCookie = jwtCookieResolver.resolveToken(request);
        if (tokenFromCookie != null) {
            log.info("Resolved token from cookie 'access-token'");
            return tokenFromCookie;
        }

        // Fallback to Authorization header
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            log.info("Resolved token from Authorization header");
            return authorization.substring(7);
        }

        log.info("No bearer token found in cookies or Authorization header");
        return null;
    }
}
