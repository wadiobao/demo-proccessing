package com.example.demo.controller;

import java.text.ParseException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.Introspect;
import com.example.demo.dto.StateResponse;
import com.example.demo.dto.authen.AuthenticationUser;
import com.example.demo.service.iservice.IAuthenticationService;
import com.nimbusds.jose.JOSEException;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {

	IAuthenticationService authenticationService;
	
	@PostMapping("/login")
	ResponseEntity<StateResponse<Object>>  authenticate(@RequestBody AuthenticationUser request){
		
		return authenticationService.authenticate(request);		
	}
	
	@GetMapping("/introspect")
	StateResponse<Object>  introspect(@CookieValue(name = "access-token", required = true) String token) throws JOSEException, ParseException{
		
		Introspect in = Introspect.builder().token(token).build();
		
		var result = authenticationService.introspect(in);
				
		return StateResponse.builder()
				.result(result)
				.build();
		
	}
	
	@PostMapping("/logout")
	StateResponse<Object>  logout(HttpServletResponse response, @CookieValue(name = "access-token", required = true) String token) throws JOSEException, ParseException{
		authenticationService.logout(response,token);
		return StateResponse.builder().build();
	}
	
	
	@PostMapping("/refresh")
	ResponseEntity<StateResponse<Object>>  refreshToken(HttpServletResponse response, @CookieValue(name = "refresh-token", required = true) String token) throws JOSEException, ParseException{
		return authenticationService.refreshToken(response,token);		
		
	}

	
}
