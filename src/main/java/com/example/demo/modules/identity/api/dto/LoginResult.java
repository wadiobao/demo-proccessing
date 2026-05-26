package com.example.demo.modules.identity.api.dto;

import org.springframework.http.ResponseCookie;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResult {
    private ResponseCookie accessTokenCookie;
    private ResponseCookie refreshTokenCookie;
    private boolean authenticated;
}
