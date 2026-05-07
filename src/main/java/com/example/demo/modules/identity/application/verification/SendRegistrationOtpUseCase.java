package com.example.demo.modules.identity.application.verification;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.example.demo.modules.identity.api.dto.UserRegistrationRequest;
import com.example.demo.modules.identity.domain.port.MailPort;
import com.example.demo.modules.identity.domain.port.VerificationPort;
import com.example.demo.utils.GeneralUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendRegistrationOtpUseCase {

    private final VerificationPort verificationPort;
    private final MailPort mailPort;
    private final GeneralUtils generalUtils;
    private final ObjectMapper mapper;

    public String execute(UserRegistrationRequest request) {
        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999));
        
        String json;
        try {
            json = mapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize registration request", e);
            throw new RuntimeException("Serialization error");
        }
        
        String hash = generalUtils.sha256(request.getEmail() + otp);
        log.info("Generating registration OTP for hash: {}", hash);
        
        verificationPort.store(hash, json, 3, TimeUnit.MINUTES);

        String subject = "Mã OTP Đăng ký";
        String body = "Mã OTP của bạn là: " + otp + " \n Mã sẽ hết hạn sau 3 phút";
        mailPort.sendSimpleMail(request.getEmail(), subject, body);
        
        return otp;
    }
}
