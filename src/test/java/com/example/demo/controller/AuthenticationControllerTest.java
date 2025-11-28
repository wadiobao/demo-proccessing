package com.example.demo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.example.demo.config.TestSecurityConfig;
import com.example.demo.dto.StateResponse;
import com.example.demo.dto.authen.AuthenticationResponse;
import com.example.demo.dto.authen.AuthenticationUser;
import com.example.demo.service.AuthenticationService;
import com.example.demo.service.OTPMailService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = AuthenticationController.class)
@Import(TestSecurityConfig.class)
class AuthenticationControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private AuthenticationService authenticationService;
	
	@MockitoBean
	private OTPMailService mailService;

	private AuthenticationUser user;
	private AuthenticationResponse response;

	@BeforeEach
	void setUp() {
		user = AuthenticationUser.builder().userName("admin").password("admin").build();
		response = AuthenticationResponse.builder().auth(true).build();
	}

	@Test
	void login_WithValidUser_ShouldReturnToken() throws JsonProcessingException, Exception {
		when(authenticationService.authenticate(any(AuthenticationUser.class))).thenReturn(ResponseEntity.ok(StateResponse.builder().result(response).build()));

		mockMvc.perform(MockMvcRequestBuilders.post("/auth/login").contentType("application/json")
				.content(objectMapper.writeValueAsString(user))).andExpect(status().isOk()).andExpect(jsonPath("$.result.auth").value(true));
	}

	}
