package com.example.demo.modules.identity.application.user;

import java.io.IOException;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.enums.ErrorCode;
import com.example.demo.exception.HandleException;
import com.example.demo.modules.identity.api.dto.UserProfileResponse;
import com.example.demo.modules.identity.domain.model.NormalUser;
import com.example.demo.modules.identity.domain.repository.IUserRepository;
import com.example.demo.utils.CloudinaryUtils;

import lombok.RequiredArgsConstructor;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class UpdateProfileUseCase {

    private final IUserRepository userRepository;
    private final CloudinaryUtils cloudinaryUtils;

    @Transactional
    public UserProfileResponse execute(MultipartFile avatar, Date birthDate) throws IOException {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        
        NormalUser user = (NormalUser) userRepository.findByUserName(name)
                .orElseThrow(() -> new HandleException(ErrorCode.USER_NOT_EXISTED));

        if (avatar != null && !avatar.isEmpty()) {
            String avatarUrl = cloudinaryUtils.uploadAvatar(avatar);
            user.setAvatarUrl(avatarUrl);
        }

        if (birthDate != null) {
            user.setDate(birthDate);
        }

        userRepository.save(user);
        
        return UserProfileResponse.builder()
                .userName(user.getUserName())
                .email(user.getEmail())
                .date(user.getDate())
                .currentTier(user.getCurrentTier().getId())
                .avatarUrl(user.getAvatarUrl())
                .reputationScore(user.getReputationScore())
                .build();
    }
}
