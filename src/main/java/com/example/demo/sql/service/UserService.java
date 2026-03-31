package com.example.demo.sql.service;

import java.io.IOException;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.enums.ErrorCode;
import com.example.demo.exception.HandleException;
import com.example.demo.sql.dto.user.ChangePasswordRequest;
import com.example.demo.sql.dto.user.ResetPasswordRequest;
import com.example.demo.sql.dto.user.UserRequest;
import com.example.demo.sql.dto.user.UserResponse;
import com.example.demo.sql.entity.NormalUser;
import com.example.demo.sql.entity.Role;
import com.example.demo.sql.entity.User;
import com.example.demo.sql.repository.NormalUserRepository;
import com.example.demo.sql.repository.RoleRepository;
import com.example.demo.sql.repository.TierRepository;
import com.example.demo.sql.repository.UserRepository;
import com.example.demo.sql.entity.Tier;
import com.example.demo.sql.service.iservice.IUserService;
import com.example.demo.utils.CloudinaryUtils;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService implements IUserService {

	UserRepository userRepository;
	NormalUserRepository normalUserRepository;
	RoleRepository roleRepository;
	TierRepository tierRepository;
	PasswordEncoder encoder;
	CloudinaryUtils cloudinaryUtils;

	@Override
	@PreAuthorize("hasRole('ADMIN')")
	public Page<User> getAll(Pageable pageable) {
		return userRepository.findAll(pageable);
	}

	@Override
	public void checkExist(UserRequest request) {
		if (userRepository.existsByUserName(request.getUserName())) {
			throw new HandleException(ErrorCode.USER_EXISTED);
		}

		if (userRepository.existsByEmail(request.getEmail())) {
			throw new HandleException(ErrorCode.EMAIL_EXISTED);
		}
	}

	@Override
	@Transactional
	public UserResponse registerUser(UserRequest request) {
		if (userRepository.existsByUserName(request.getUserName())) {
			throw new HandleException(ErrorCode.USER_EXISTED);
		}

		Role role = roleRepository.findById("USER")
				.orElseThrow(() -> new HandleException(ErrorCode.UNCATEGORIZED_EXCEPTION));

		Tier tier = tierRepository.findById("CONTRIBUTOR")
				.orElseThrow(() -> new HandleException(ErrorCode.UNCATEGORIZED_EXCEPTION));

		NormalUser user = NormalUser.builder()
				.userName(request.getUserName())
				.password(encoder.encode(request.getPassword()))
				.email(request.getEmail())
				.date(request.getDate())
				.roles(Set.of(role))
				.reputationScore(0)
				.currentTier(tier)
				.build();
		normalUserRepository.save(user);

		return UserResponse.builder()
				.userName(request.getUserName())
				.email(request.getEmail())
				.date(request.getDate())
				.roles(Set.of("USER"))
				.build();
	}

	@Override
	@PreAuthorize("hasRole('ADMIN')")
	@Transactional
	public UserResponse getInfor(String username) {
		if (!userRepository.existsByUserName(username)) {
			throw new HandleException(ErrorCode.USER_NOT_EXISTED);
		}
		User user = userRepository.findByUserName(username).orElseThrow();
		return UserResponse.builder()
				.userName(username)
				.email(user.getEmail())
				.date(user.getDate())
				.build();
	}

	@Override
	@Transactional
	public UserResponse myInfor() {
		var contenxt = SecurityContextHolder.getContext();
		String name = contenxt.getAuthentication().getName();

		User user = userRepository.findByUserName(name)
				.orElseThrow(() -> new HandleException(ErrorCode.USER_NOT_EXISTED));

		return UserResponse.builder()
				.userName(user.getUserName())
				.email(user.getEmail())
				.date(user.getDate())
				.roles(user.getRoles().stream().map(Role::getName).collect(java.util.stream.Collectors.toSet()))
				.build();
	}

	@Override
	@Transactional
	public void resetPassword(ResetPasswordRequest request) {
		User user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new HandleException(ErrorCode.USER_NOT_EXISTED));
		user.setPassword(encoder.encode(request.getNewPassword()));
		userRepository.save(user);
	}

	@Override
	@Transactional
	public UserResponse updateProfile(MultipartFile avatar) throws IOException {
		var context = SecurityContextHolder.getContext();
		String name = context.getAuthentication().getName();
		User user = userRepository.findByUserName(name)
				.orElseThrow(() -> new HandleException(ErrorCode.USER_NOT_EXISTED));

		if (avatar != null && !avatar.isEmpty()) {
			String avatarUrl = cloudinaryUtils.uploadAvatar(avatar);
			user.setAvatarUrl(avatarUrl);
		}

		userRepository.save(user);
		return UserResponse.builder()
				.userName(user.getUserName())
				.email(user.getEmail())
				.date(user.getDate())
				.avatarUrl(user.getAvatarUrl())
				.roles(user.getRoles().stream().map(Role::getName).collect(java.util.stream.Collectors.toSet()))
				.build();
	}

	@Override
	@Transactional
	public void changePassword(ChangePasswordRequest request) {
		var context = SecurityContextHolder.getContext();
		String name = context.getAuthentication().getName();
		User user = userRepository.findByUserName(name)
				.orElseThrow(() -> new HandleException(ErrorCode.USER_NOT_EXISTED));

		if (!encoder.matches(request.getOldPassword(), user.getPassword())) {
			throw new HandleException(ErrorCode.PASSWORD_INVALID);
		}

		user.setPassword(encoder.encode(request.getNewPassword()));
		userRepository.save(user);
	}

	@Override
	@Transactional
	@PreAuthorize("hasRole('ADMIN')")
	public void updateRole(Long userId, Set<String> roles) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new HandleException(ErrorCode.USER_NOT_EXISTED));
		
		Set<Role> newRoles = roles.stream()
				.map(r -> roleRepository.findById(r).orElseThrow(() -> new HandleException(ErrorCode.UNCATEGORIZED_EXCEPTION)))
				.collect(java.util.stream.Collectors.toSet());
				
		user.setRoles(newRoles);
		userRepository.save(user);
	}

	@Override
	@Transactional
	@PreAuthorize("hasRole('ADMIN')")
	public void deleteUser(Long userId) {
		if (!userRepository.existsById(userId)) {
			throw new HandleException(ErrorCode.USER_NOT_EXISTED);
		}
		userRepository.deleteById(userId);
	}

}
