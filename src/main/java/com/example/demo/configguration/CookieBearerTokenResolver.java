package com.example.demo.configguration;

import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class CookieBearerTokenResolver implements BearerTokenResolver {
    
    private final JwtCookieResolver jwtCookieResolver;
    
    public CookieBearerTokenResolver(JwtCookieResolver jwtCookieResolver) {
        this.jwtCookieResolver = jwtCookieResolver;
    }
    
    @Override
    public String resolve(HttpServletRequest request) {
        // Try to get token from cookie first
        String tokenFromCookie = jwtCookieResolver.resolveToken(request);
        if (tokenFromCookie != null) {
            return tokenFromCookie;
        }
        
        // Fallback to Authorization header
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        
        return null;
    }
}
