package com.example.demo.modules.identity.application.mapper;

import org.mapstruct.Mapper;

import com.example.demo.modules.identity.api.dto.UserProfileResponse;
import com.example.demo.modules.identity.api.dto.UserRegistrationRequest;
import com.example.demo.modules.identity.domain.model.NormalUser;
import com.example.demo.modules.identity.domain.model.User;


@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserRegistrationRequest request);

    default UserProfileResponse toResponse(User user) {
        if (user == null) return null;
        if (user instanceof NormalUser) return toResponse((NormalUser) user);
        return toBaseResponse(user);
    }

    UserProfileResponse toBaseResponse(User user);

    @org.mapstruct.Mapping(target = "currentTier", source = "currentTier.id")
    UserProfileResponse toResponse(NormalUser user);
}
