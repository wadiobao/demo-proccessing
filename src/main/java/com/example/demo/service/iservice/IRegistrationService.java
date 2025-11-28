package com.example.demo.service.iservice;

import com.example.demo.dto.user.UserRequest;
import com.example.demo.dto.user.UserResponse;

public interface IRegistrationService {
    String generateRegistrationToken(UserRequest userRequest);
    UserResponse verifyRegistration(String email, String otp);
}
