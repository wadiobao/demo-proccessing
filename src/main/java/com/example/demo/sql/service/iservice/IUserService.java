package com.example.demo.sql.service.iservice;

import java.io.IOException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.sql.dto.user.ChangePasswordRequest;
import com.example.demo.sql.dto.user.ResetPasswordRequest;
import com.example.demo.sql.dto.user.UserRequest;
import com.example.demo.sql.dto.user.UserResponse;
import com.example.demo.sql.entity.User;

public interface IUserService {

    void checkExist(UserRequest request);

    Page<User> getAll(Pageable pageable);

    UserResponse registerUser(UserRequest request);

    UserResponse getInfor(String username);

    UserResponse myInfor();

    void resetPassword(ResetPasswordRequest request);

    UserResponse updateProfile(MultipartFile avatar) throws IOException;

    void changePassword(ChangePasswordRequest request);
}
