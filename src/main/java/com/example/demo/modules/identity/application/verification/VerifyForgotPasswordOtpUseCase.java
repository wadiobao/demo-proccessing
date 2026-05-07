package com.example.demo.modules.identity.application.verification;

import org.springframework.stereotype.Service;

import com.example.demo.enums.ErrorCode;
import com.example.demo.exception.HandleException;
import com.example.demo.modules.identity.domain.port.VerificationPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VerifyForgotPasswordOtpUseCase {

    private final VerificationPort verificationPort;

    public void execute(String email, String otp) {
        String key = email + "_RESET";
        String dbOtp = verificationPort.get(key)
                .orElseThrow(() -> new HandleException(ErrorCode.INVALID_OTP));

        if (!otp.equals(dbOtp)) {
            throw new HandleException(ErrorCode.INVALID_OTP);
        }

        verificationPort.delete(key);
    }
}
