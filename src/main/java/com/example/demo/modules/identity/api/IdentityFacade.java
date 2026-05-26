package com.example.demo.modules.identity.api;

import java.io.IOException;
import java.text.ParseException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.modules.identity.api.dto.GoogleLoginRequest;
import com.example.demo.modules.identity.api.dto.IntrospectResponse;
import com.example.demo.modules.identity.api.dto.LoginRequest;
import com.example.demo.modules.identity.api.dto.LoginResult;
import com.example.demo.modules.identity.api.dto.UserProfileResponse;
import com.example.demo.modules.identity.api.dto.UserRegistrationRequest;
import com.example.demo.modules.identity.application.auth.AdminLoginUseCase;
import com.example.demo.modules.identity.application.auth.GoogleOAuthLoginUseCase;
import com.example.demo.modules.identity.application.auth.LogoutUseCase;
import com.example.demo.modules.identity.application.auth.RefreshTokenUseCase;
import com.example.demo.modules.identity.application.auth.RegisterUseCase;
import com.example.demo.modules.identity.application.auth.UserLoginUseCase;
import com.example.demo.modules.identity.application.management.DeleteUserUseCase;
import com.example.demo.modules.identity.application.management.GetAllUsersUseCase;
import com.example.demo.modules.identity.application.user.ChangePasswordUseCase;
import com.example.demo.modules.identity.application.user.GetMyProfileUseCase;
import com.example.demo.modules.identity.application.user.ResetPasswordUseCase;
import com.example.demo.modules.identity.application.user.UpdateProfileUseCase;
import com.example.demo.modules.identity.application.verification.SendForgotPasswordOtpUseCase;
import com.example.demo.modules.identity.application.verification.SendRegistrationOtpUseCase;
import com.example.demo.modules.identity.application.verification.VerifyForgotPasswordOtpUseCase;
import com.example.demo.modules.identity.application.verification.VerifyRegistrationOtpUseCase;
import com.example.demo.modules.identity.domain.model.User;
import com.example.demo.modules.identity.infrastructure.security.JwtUtils;
import com.nimbusds.jose.JOSEException;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Unified entry point for all Identity related operations.
 */
@Component
@RequiredArgsConstructor
public class IdentityFacade {

    private final UserLoginUseCase userLoginUseCase;
    private final AdminLoginUseCase adminLoginUseCase;
    private final RegisterUseCase registerUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final GetMyProfileUseCase getMyProfileUseCase;
    private final com.example.demo.modules.identity.application.user.GetUserProfileByUsernameUseCase getUserProfileByUsernameUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final UpdateProfileUseCase updateProfileUseCase;
    private final SendRegistrationOtpUseCase sendRegistrationOtpUseCase;
    private final VerifyRegistrationOtpUseCase verifyRegistrationOtpUseCase;
    private final SendForgotPasswordOtpUseCase sendForgotPasswordOtpUseCase;
    private final VerifyForgotPasswordOtpUseCase verifyForgotPasswordOtpUseCase;
    private final GetAllUsersUseCase getAllUsersUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final GoogleOAuthLoginUseCase googleOAuthLoginUseCase;
    private final JwtUtils jwtUtils;

    public UserProfileResponse getUserByUsername(String username) {
        return getUserProfileByUsernameUseCase.execute(username);
    }

    public LoginResult login(LoginRequest request) {
        return userLoginUseCase.execute(request);
    }
    
    public LoginResult googleLogin(GoogleLoginRequest request) {
        return googleOAuthLoginUseCase.execute(request);
    }

    public LoginResult adminLogin(LoginRequest request) {
        return adminLoginUseCase.execute(request);
    }

    public UserProfileResponse register(UserRegistrationRequest request) {
        return registerUseCase.execute(request);
    }

    public RefreshTokenUseCase.RefreshResult refreshToken(String token) throws JOSEException, ParseException {
        return refreshTokenUseCase.execute(token);
    }

    public void logout(HttpServletResponse response, String token) throws JOSEException, ParseException {
        logoutUseCase.execute(response, token);
    }

    public UserProfileResponse getMyProfile() {
        return getMyProfileUseCase.execute();
    }

    public void changePassword(String oldPassword, String newPassword) {
        changePasswordUseCase.execute(oldPassword, newPassword);
    }

    public void resetPassword(String email, String newPassword) {
        resetPasswordUseCase.execute(email, newPassword);
    }

    public UserProfileResponse updateProfile(MultipartFile avatar) throws IOException {
        return updateProfileUseCase.execute(avatar);
    }

    public String sendRegistrationOtp(UserRegistrationRequest request) {
        return sendRegistrationOtpUseCase.execute(request);
    }

    public UserProfileResponse verifyRegistrationOtp(String email, String otp) {
        return verifyRegistrationOtpUseCase.execute(email, otp);
    }

    public String sendForgotPasswordOtp(String email) {
        return sendForgotPasswordOtpUseCase.execute(email);
    }

    public void verifyForgotPasswordOtp(String email, String otp) {
        verifyForgotPasswordOtpUseCase.execute(email, otp);
    }

    public Page<User> getAllUsers(Pageable pageable) {
        return getAllUsersUseCase.execute(pageable);
    }

    public void deleteUser(Long userId) {
        deleteUserUseCase.execute(userId);
    }

    public IntrospectResponse introspect(String token) {
        boolean isValid = true;
        try {
            jwtUtils.verifyToken(token);
        } catch (Exception e) {
            isValid = false;
        }
        return IntrospectResponse.builder().valid(isValid).build();
    }
}
