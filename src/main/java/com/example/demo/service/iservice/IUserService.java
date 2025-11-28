package com.example.demo.service.iservice;

import java.util.List;

import com.example.demo.dto.user.UserRequest;
import com.example.demo.dto.user.UserResponse;
import com.example.demo.sql.entity.User;

public interface IUserService {
	
	void checkExist(UserRequest request);
	
    List<User> getAll();

    UserResponse registerUser(UserRequest request);

    UserResponse getInfor(String username);

    UserResponse myInfor();
}
