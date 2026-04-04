package com.example.demo.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import com.example.demo.enums.ErrorCode;
import com.example.demo.exception.HandleException;

/**
 * Utility class for working with Spring Security Context and JWT Claims.
 */
public class SecurityUtils {

    /**
     * Extracts the User ID (uid) from the currently authenticated JWT token.
     * 
     * @return User ID as Long
     * @throws HandleException if context is unauthenticated or token doesn't have a valid uid claim
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new HandleException(ErrorCode.UNAUTHENTICATED);
        }

        Object principal = authentication.getPrincipal();
        
        if (principal instanceof Jwt jwt) {
            Object uidObj = jwt.getClaim("uid");
            if (uidObj instanceof Number number) {
                return number.longValue();
            } else if (uidObj instanceof String str) {
                try {
                    return Long.parseLong(str);
                } catch (NumberFormatException e) {
                    // fallthrough to throw UNAUTHENTICATED
                }
            }
        }
        
        // Token might be old (doesn't contain uid) or invalid type
        throw new HandleException(ErrorCode.UNAUTHENTICATED);
    }
    
    /**
     * Extracts the User Type (usr_typ) from the currently authenticated JWT token.
     * 
     * @return User type string (e.g., "ADMIN", "NORMAL")
     * @throws HandleException if context is unauthenticated
     */
    public static String getCurrentUserType() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new HandleException(ErrorCode.UNAUTHENTICATED);
        }

        Object principal = authentication.getPrincipal();
        
        if (principal instanceof Jwt jwt) {
            String usrTyp = jwt.getClaim("usr_typ");
            if (usrTyp != null) {
                return usrTyp;
            }
        }
        
        throw new HandleException(ErrorCode.UNAUTHENTICATED);
    }
}
