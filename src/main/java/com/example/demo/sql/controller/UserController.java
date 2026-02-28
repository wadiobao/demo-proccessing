package com.example.demo.sql.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import com.example.demo.dto.StateResponse;
import com.example.demo.sql.dto.otp.RegistrationOtpRequest;
import com.example.demo.sql.dto.otp.RegistrationOtpRequest;
import com.example.demo.sql.dto.user.ChangePasswordRequest;
import com.example.demo.sql.dto.user.ForgotPasswordRequest;
import com.example.demo.sql.dto.user.ResetPasswordRequest;
import com.example.demo.sql.dto.user.UserRequest;
import com.example.demo.sql.service.iservice.IOTPMailService;
import com.example.demo.sql.service.iservice.IUserService;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

	IUserService service;
	IOTPMailService mailService;

	@GetMapping
	public ResponseEntity<StateResponse<Object>> getAll(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		var authen = SecurityContextHolder.getContext().getAuthentication();
		log.info(authen.getName());
		authen.getAuthorities().forEach(granted -> log.info(granted.getAuthority()));
		return ResponseEntity.ok(StateResponse.builder().result(service.getAll(PageRequest.of(page, size))).build());
	}

	@PostMapping("/register")
	public ResponseEntity<StateResponse<Object>> register(@RequestBody @Valid UserRequest request) {
		service.checkExist(request);
		mailService.generateAndSendOtp(request);
		return ResponseEntity.ok(StateResponse.builder().message("OTP sent").build());
	}

	@PostMapping("/register/otp")
	ResponseEntity<StateResponse<Object>> verifYOtp(@RequestBody RegistrationOtpRequest otpRequest) {
		return ResponseEntity.ok(StateResponse.builder()
				.result(mailService.verifyOtp(otpRequest.getEmail(), otpRequest.getOtp())).build());
	}

	@GetMapping("/{username}")
	public ResponseEntity<StateResponse<Object>> getUser(@PathVariable("username") String username) {
		return ResponseEntity.ok(StateResponse.builder().result(service.getInfor(username)).build());

	}

	@GetMapping("/myinfor")
	public ResponseEntity<StateResponse<Object>> getMyInfor() {
		return ResponseEntity.ok(StateResponse.builder().result(service.myInfor()).build());
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<StateResponse<Object>> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
		mailService.sendForgotPasswordOtp(request.getEmail());
		return ResponseEntity.ok(StateResponse.builder().message("OTP khôi phục đã được gửi").build());
	}

	@PostMapping("/reset-password")
	public ResponseEntity<StateResponse<Object>> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
		mailService.verifyOtp(request.getEmail(), request.getOtp());
		service.resetPassword(request);
		return ResponseEntity.ok(StateResponse.builder().message("Mật khẩu đã được đặt lại thành công").build());
	}

	@PutMapping("/profile")
	public ResponseEntity<StateResponse<Object>> updateProfile(
			@RequestPart(value = "avatar", required = false) MultipartFile avatar) throws IOException {
		return ResponseEntity.ok(StateResponse.builder().result(service.updateProfile(avatar)).build());
	}

	@PutMapping("/change-password")
	public ResponseEntity<StateResponse<Object>> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
		service.changePassword(request);
		return ResponseEntity.ok(StateResponse.builder().message("Đổi mật khẩu thành công").build());
	}
}
