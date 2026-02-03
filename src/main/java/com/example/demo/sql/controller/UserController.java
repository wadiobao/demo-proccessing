package com.example.demo.sql.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.StateResponse;
import com.example.demo.sql.dto.otp.RegistrationOtpRequest;
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
}
