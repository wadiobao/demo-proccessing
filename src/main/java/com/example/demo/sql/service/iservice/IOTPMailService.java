package com.example.demo.sql.service.iservice;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.StateResponse;
import com.example.demo.sql.dto.user.UserRequest;
import com.example.demo.sql.dto.user.UserResponse;

import jakarta.mail.MessagingException;

public interface IOTPMailService {
    String generateAndSendOtp(UserRequest request);

    UserResponse verifyOtp(String email, String otp);

    StateResponse<Object> sendDonatetoMyMail(String name, String note, MultipartFile file) throws IOException, MessagingException;

    StateResponse<Object> sendBugtoMyMail(String name, String note) throws IOException, MessagingException;
}
