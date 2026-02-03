package com.example.demo.sql.service.iservice;

import com.example.demo.sql.dto.user.UserRequest;
import com.example.demo.sql.dto.user.UserResponse;

public interface IRegistrationService {
    String generateRegistrationToken(UserRequest userRequest);
    UserResponse verifyRegistration(String email, String otp);
}
