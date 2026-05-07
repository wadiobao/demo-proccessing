package com.example.demo.modules.identity.api;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.identity.api.dto.UserProfileResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final IdentityFacade identityFacade;

    @GetMapping("/me")
    public ResponseEntity<StateResponse<UserProfileResponse>> getMyProfile() {
        return ResponseEntity.ok(StateResponse.<UserProfileResponse>builder()
                .result(identityFacade.getMyProfile())
                .build());
    }

    @PostMapping("/password/change")
    public ResponseEntity<StateResponse<Void>> changePassword(@RequestParam String oldPassword, @RequestParam String newPassword) {
        identityFacade.changePassword(oldPassword, newPassword);
        return ResponseEntity.ok(StateResponse.<Void>builder().build());
    }

    @PostMapping("/password/reset/otp")
    public ResponseEntity<StateResponse<String>> sendResetPasswordOtp(@RequestParam String email) {
        return ResponseEntity.ok(StateResponse.<String>builder()
                .result(identityFacade.sendForgotPasswordOtp(email))
                .build());
    }

    @PostMapping("/password/reset/verify")
    public ResponseEntity<StateResponse<Void>> verifyResetPasswordOtp(@RequestParam String email, @RequestParam String otp) {
        identityFacade.verifyForgotPasswordOtp(email, otp);
        return ResponseEntity.ok(StateResponse.<Void>builder().build());
    }

    @PostMapping("/profile/update")
    public ResponseEntity<StateResponse<UserProfileResponse>> updateProfile(@RequestParam(required = false) MultipartFile avatar) throws IOException {
        return ResponseEntity.ok(StateResponse.<UserProfileResponse>builder()
                .result(identityFacade.updateProfile(avatar))
                .build());
    }

    @GetMapping("/{username}")
    public ResponseEntity<StateResponse<Object>> getUser(@PathVariable("username") String username) {
        return ResponseEntity.ok(StateResponse.builder().result(identityFacade.getUserByUsername(username)).build());
    }
}
