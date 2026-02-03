package com.example.demo.sql.service.iservice;

import java.text.ParseException;

import org.springframework.http.ResponseEntity;

import com.example.demo.sql.dto.Introspect;
import com.example.demo.sql.dto.IntrospectResponse;
import com.example.demo.dto.StateResponse;
import com.example.demo.sql.dto.authen.AuthenticationUser;
import com.nimbusds.jose.JOSEException;

import jakarta.servlet.http.HttpServletResponse;

public interface IAuthenticationService {
    ResponseEntity<StateResponse<Object>> authenticate(AuthenticationUser authenticationUser);

    IntrospectResponse introspect(Introspect introspect) throws JOSEException, ParseException;

    ResponseEntity<StateResponse<Object>> refreshToken(HttpServletResponse response, String token) throws JOSEException, ParseException;

    void logout(HttpServletResponse response, String token) throws JOSEException, ParseException;
}
