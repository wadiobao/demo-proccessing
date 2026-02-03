package com.example.demo.sql.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.sql.dto.user.UserRequest;
import com.example.demo.sql.dto.user.UserResponse;
import com.example.demo.enums.ErrorCode;
import com.example.demo.enums.Role;
import com.example.demo.exception.HandleException;
import com.example.demo.sql.service.iservice.IUserService;
import com.example.demo.sql.entity.User;
import com.example.demo.sql.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService implements IUserService {

	UserRepository userRepository;
	PasswordEncoder encoder;

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

		Set<String> role = new HashSet<String>();
		role.add(Role.USER.name());

		User user = User.builder()
				.userName(request.getUserName())
				.password(encoder.encode(request.getPassword()))
				.email(request.getEmail())
				.date(request.getDate())
				.roles(role)
				.build();
		userRepository.save(user);

		return UserResponse.builder()
				.userName(request.getUserName())
				.email(request.getEmail())
				.date(request.getDate())
				.roles(role)
				.build();
	}

	@Override
	@PreAuthorize("hasRole('ADMIN')")
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
	public UserResponse myInfor() {
		var contenxt = SecurityContextHolder.getContext();
		String name = contenxt.getAuthentication().getName();

		User user = userRepository.findByUserName(name)
				.orElseThrow(() -> new HandleException(ErrorCode.USER_NOT_EXISTED));

		return UserResponse.builder()
				.userName(user.getUserName())
				.email(user.getEmail())
				.date(user.getDate())
				.roles(user.getRoles())
				.build();
	}

}
