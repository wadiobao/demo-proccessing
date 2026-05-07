package com.example.demo.modules.identity.application.verification;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.example.demo.modules.identity.domain.port.MailPort;
import com.example.demo.modules.identity.domain.port.VerificationPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendForgotPasswordOtpUseCase {

    private final VerificationPort verificationPort;
    private final MailPort mailPort;

    public String execute(String email) {
        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999));
        
        String key = email + "_RESET";
        verificationPort.store(key, otp, 5, TimeUnit.MINUTES);

        String subject = "Mã khôi phục mật khẩu";
        String body = "Mã OTP để khôi phục mật khẩu của bạn là: " + otp + " \n Mã sẽ hết hạn sau 5 phút";
        mailPort.sendSimpleMail(email, subject, body);

        return otp;
    }
}
