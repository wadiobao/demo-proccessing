package com.example.demo.mapper;

import org.mapstruct.Mapper;

import com.example.demo.sql.dto.user.UserRequest;
import com.example.demo.sql.entity.User;


@Mapper(componentModel = "spring")
public interface UserMapper {
	User toUser(UserRequest request);
}
