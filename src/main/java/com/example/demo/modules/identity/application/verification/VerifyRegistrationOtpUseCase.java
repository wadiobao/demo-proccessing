package com.example.demo.modules.identity.application.verification;

import org.springframework.stereotype.Service;

import com.example.demo.enums.ErrorCode;
import com.example.demo.exception.HandleException;
import com.example.demo.modules.identity.api.dto.UserProfileResponse;
import com.example.demo.modules.identity.api.dto.UserRegistrationRequest;
import com.example.demo.modules.identity.application.auth.RegisterUseCase;
import com.example.demo.modules.identity.domain.port.VerificationPort;
import com.example.demo.modules.identity.domain.repository.IUserRepository;
import com.example.demo.utils.GeneralUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerifyRegistrationOtpUseCase {

    private final VerificationPort verificationPort;
    private final RegisterUseCase registerUseCase;
    private final IUserRepository userRepository;
    private final GeneralUtils generalUtils;
    private final ObjectMapper mapper;

    public UserProfileResponse execute(String email, String otp) {
        String hash = generalUtils.sha256(email + otp);
        log.info("Verifying registration OTP for hash: {}", hash);

        // Pre-check if user already registered (to avoid confusing invalid otp error)
        if (userRepository.existsByEmail(email)) {
            throw new HandleException(ErrorCode.USER_EXISTED);
        }

        String json = verificationPort.get(hash)
                .orElseThrow(() -> new HandleException(ErrorCode.INVALID_OTP));

        try {
            UserRegistrationRequest request = mapper.readValue(json, UserRegistrationRequest.class);
            verificationPort.delete(hash);
            return registerUseCase.execute(request);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize registration request from redis", e);
            throw new RuntimeException("Data integrity error");
        } finally {
            verificationPort.delete(hash);
        }
    }
}
