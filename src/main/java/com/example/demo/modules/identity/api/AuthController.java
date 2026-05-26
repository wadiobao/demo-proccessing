package com.example.demo.modules.identity.api;

import java.text.ParseException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.identity.api.dto.GoogleLoginRequest;
import com.example.demo.modules.identity.api.dto.LoginRequest;
import com.example.demo.modules.identity.api.dto.LoginResult;
import com.example.demo.modules.identity.api.dto.UserProfileResponse;
import com.example.demo.modules.identity.api.dto.UserRegistrationRequest;
import com.example.demo.modules.identity.application.auth.RefreshTokenUseCase;
import com.nimbusds.jose.JOSEException;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IdentityFacade identityFacade;

    @PostMapping("/login")
    public ResponseEntity<StateResponse<Object>> login(@RequestBody LoginRequest request) {
        LoginResult result = identityFacade.login(request);
        return createAuthResponse(result.getAccessTokenCookie(), result.getRefreshTokenCookie(),
                result.isAuthenticated());
    }

    @PostMapping("/admin/login")
    public ResponseEntity<StateResponse<Object>> adminLogin(@RequestBody LoginRequest request) {
        LoginResult result = identityFacade.adminLogin(request);
        return createAuthResponse(result.getAccessTokenCookie(), result.getRefreshTokenCookie(),
                result.isAuthenticated());
    }

    @PostMapping("/register/otp")
    public ResponseEntity<StateResponse<Object>> sendRegisterOtp(@RequestBody UserRegistrationRequest request) {
        String otp = identityFacade.sendRegistrationOtp(request);
        return ResponseEntity.ok(StateResponse.builder().result(otp).build());
    }

    @PostMapping("/register/verify")
    public ResponseEntity<StateResponse<Object>> verifyRegisterOtp(@RequestParam String email,
            @RequestParam String otp) {
        UserProfileResponse response = identityFacade.verifyRegistrationOtp(email, otp);
        return ResponseEntity.ok(StateResponse.builder().result(response).build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<StateResponse<Object>> refresh(@CookieValue("refresh-token") String token)
            throws JOSEException, ParseException {
        RefreshTokenUseCase.RefreshResult result = identityFacade.refreshToken(token);
        return createAuthResponse(result.getAccessTokenCookie(), result.getRefreshTokenCookie(),
                result.isAuthenticated());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response, @CookieValue("refresh-token") String token)
            throws JOSEException, ParseException {
        identityFacade.logout(response, token);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/google")
    public ResponseEntity<StateResponse<Object>> googleLogin(@RequestBody GoogleLoginRequest request)
            throws JOSEException, ParseException {
        LoginResult result = identityFacade.googleLogin(request);
        return createAuthResponse(result.getAccessTokenCookie(), result.getRefreshTokenCookie(),
                result.isAuthenticated());
    }

    private ResponseEntity<StateResponse<Object>> createAuthResponse(ResponseCookie access,ResponseCookie refresh, boolean auth) {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, access.toString())
                .header(HttpHeaders.SET_COOKIE, refresh.toString())
                .body(StateResponse.builder()
                        .result(java.util.Map.of("auth", auth))
                        .build());
    }
}
