package com.example.demo.controller;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.StateResponse;
import com.example.demo.dto.otp.RegistrationOtpRequest;
import com.example.demo.dto.user.UserRequest;
import com.example.demo.service.iservice.IOTPMailService;
import com.example.demo.service.iservice.IUserService;
import com.example.demo.sql.entity.User;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

	
	IUserService service;
	IOTPMailService mailService;
	
	@GetMapping
	public List<User> getAllUser() {
		var authen = SecurityContextHolder.getContext().getAuthentication();
		log.info(authen.getName());
		authen.getAuthorities().forEach(granted -> log.info(granted.getAuthority()));
		return service.getAll();
	}

	@PostMapping("/register")
	public StateResponse<Object> register(@RequestBody @Valid UserRequest request) {
		service.checkExist(request);
		mailService.generateAndSendOtp(request);
		return StateResponse.builder().message("OTP sent").build();
	}
	
	
	@PostMapping("/register/otp")
	StateResponse<Object> verifYOtp(@RequestBody RegistrationOtpRequest otpRequest){
		return StateResponse.builder()
				.result(mailService.verifyOtp(otpRequest.getEmail(), otpRequest.getOtp()))
				.build();
	}
	
	
	@GetMapping("/{username}")
	public StateResponse<Object> getUser(@PathVariable("username") String username){
		return StateResponse.builder()
				.result(service.getInfor(username))
				.build();
		
	}
	
	@GetMapping("/myinfor")
	public StateResponse<Object> getMyInfor(){
		return StateResponse.builder()
				.result(service.myInfor())
				.build();
		
	}

	
}
