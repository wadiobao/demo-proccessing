package com.example.demo.modules.identity.application.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.enums.ErrorCode;
import com.example.demo.exception.HandleException;
import com.example.demo.modules.identity.api.dto.UserProfileResponse;
import com.example.demo.modules.identity.api.dto.UserRegistrationRequest;
import com.example.demo.modules.identity.domain.model.NormalUser;
import com.example.demo.modules.identity.domain.model.Role;
import com.example.demo.modules.identity.domain.model.Tier;
import com.example.demo.modules.identity.domain.repository.IRoleRepository;
import com.example.demo.modules.identity.domain.repository.ITierRepository;
import com.example.demo.modules.identity.domain.repository.IUserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegisterUseCase {

    private final IUserRepository userRepository;
    private final IRoleRepository roleRepository;
    private final ITierRepository tierRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserProfileResponse execute(UserRegistrationRequest request) {
        if (userRepository.existsByUserName(request.getUserName())) {
            throw new HandleException(ErrorCode.USER_EXISTED);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new HandleException(ErrorCode.EMAIL_EXISTED);
        }

        Role role = roleRepository.findById("USER")
                .orElseThrow(() -> new HandleException(ErrorCode.UNCATEGORIZED_EXCEPTION));

        Tier tier = tierRepository.findById("MEMBER")
                .orElseThrow(() -> new HandleException(ErrorCode.UNCATEGORIZED_EXCEPTION));

        NormalUser user = NormalUser.builder()
                .userName(request.getUserName())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .date(request.getDate())
                .role(role)
                .reputationScore(0)
                .currentTier(tier)
                .build();

        // userRepository.save() accepts User (parent type), NormalUser is persisted
        // via UserPersistenceAdapter which delegates to SpringDataJpaUserRepository
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
