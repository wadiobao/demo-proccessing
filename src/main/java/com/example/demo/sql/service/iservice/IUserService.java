package com.example.demo.sql.service.iservice;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.demo.sql.dto.user.UserRequest;
import com.example.demo.sql.dto.user.UserResponse;
import com.example.demo.sql.entity.User;

public interface IUserService {

    void checkExist(UserRequest request);

    Page<User> getAll(Pageable pageable);

    UserResponse registerUser(UserRequest request);

    UserResponse getInfor(String username);

    UserResponse myInfor();
}
