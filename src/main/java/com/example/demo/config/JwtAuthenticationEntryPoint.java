package com.example.demo.config;

import java.io.IOException;

import javax.print.attribute.standard.Media;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import com.example.demo.dto.StateResponse;
import com.example.demo.enums.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		ErrorCode code = ErrorCode.UNAUTHENTICATED;
		
		response.setStatus(code.getHttpStatusCode().value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE );
		
		StateResponse<?> stateResponse = StateResponse.builder().code(code.getCode()).message(code.getMessage()).build();
		
		ObjectMapper mapper = new ObjectMapper(); 
		
		response.getWriter().write(mapper.writeValueAsString(stateResponse));
		response.flushBuffer();
		
	}

}
