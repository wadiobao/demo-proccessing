package com.example.demo.sql.service;

import java.text.ParseException;
import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.StateResponse;
import com.example.demo.enums.ErrorCode;
import com.example.demo.exception.HandleException;
import com.example.demo.sql.dto.Introspect;
import com.example.demo.sql.dto.IntrospectResponse;
import com.example.demo.sql.dto.authen.AuthenticationResponse;
import com.example.demo.sql.dto.authen.AuthenticationUser;
import com.example.demo.sql.entity.Admin;
import com.example.demo.sql.entity.InvalidatedToken;
import com.example.demo.sql.entity.NormalUser;
import com.example.demo.sql.entity.User;
import com.example.demo.sql.repository.InvalidatedTokenRepository;
import com.example.demo.sql.repository.UserRepository;
import com.example.demo.sql.service.iservice.IAuthenticationService;
import com.example.demo.utils.JwtUtils;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService implements IAuthenticationService {

	UserRepository repository;
	InvalidatedTokenRepository invalidatedTokenRepository;
	JwtUtils jwtUtils;

	@NonFinal
	@Value("${demo.secret.key}")
	String SIGN_KEY;

	@NonFinal
	@Value("${demo.time.token.refresh}")
	int REFRESH_TiME;

	@NonFinal
	@Value("${demo.time.token.access}")
	int ACCESS_TiME;

	@Override
	@Transactional
	public ResponseEntity<StateResponse<Object>> authenticate(AuthenticationUser authenticationUser) {
		Optional<User> option = repository.findByUserName(authenticationUser.getUserName());

		User user = option.orElseThrow(() -> new HandleException(ErrorCode.USER_NOT_EXISTED));
		if (!(user instanceof NormalUser)) {
			throw new HandleException(ErrorCode.UNAUTHORIZED);
		}

		PasswordEncoder encoder = new BCryptPasswordEncoder(10);

		boolean auth = encoder.matches(authenticationUser.getPassword(), user.getPassword());

		if (!auth) {
			throw new HandleException(ErrorCode.UNAUTHENTICATED);
		}

		String accessToken = jwtUtils.generateToken(user, false);
		String refreshToken = jwtUtils.generateToken(user, true);

		ResponseCookie accessCookie = jwtUtils.generateAccessCookie(accessToken);
		ResponseCookie refreshCookie = jwtUtils.generateRefreshCookie(refreshToken);

		NormalUser normalUser = (NormalUser) user;
		int reputation = normalUser.getReputationScore();
		String tier = (normalUser.getCurrentTier() != null) ? normalUser.getCurrentTier().getId() : "NONE";

		ResponseEntity<StateResponse<Object>> responseEntity = ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, accessCookie.toString())
				.header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
				.body(StateResponse.builder()
						.result(AuthenticationResponse.builder()
								.auth(auth)
								.build())
						.build());

		return responseEntity;

	}

	@Override
	@Transactional
	public ResponseEntity<StateResponse<Object>> adminAuthenticate(AuthenticationUser authenticationUser) {
		Optional<User> option = repository.findByUserName(authenticationUser.getUserName());

		User user = option.orElseThrow(() -> new HandleException(ErrorCode.USER_NOT_EXISTED));
		if (!(user instanceof Admin)) {
			throw new HandleException(ErrorCode.UNAUTHORIZED);
		}

		PasswordEncoder encoder = new BCryptPasswordEncoder(10);

		boolean auth = encoder.matches(authenticationUser.getPassword(), user.getPassword());

		if (!auth) {
			throw new HandleException(ErrorCode.UNAUTHENTICATED);
		}

		String accessToken = jwtUtils.generateToken(user, false);
		String refreshToken = jwtUtils.generateToken(user, true);

		ResponseCookie accessCookie = jwtUtils.generateAccessCookie(accessToken);
		ResponseCookie refreshCookie = jwtUtils.generateRefreshCookie(refreshToken);

		ResponseEntity<StateResponse<Object>> responseEntity = ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, accessCookie.toString())
				.header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
				.body(StateResponse.builder()
						.result(AuthenticationResponse.builder()
								.auth(auth)
								.build())
						.build());

		return responseEntity;

	}

	@Override
	public IntrospectResponse introspect(Introspect introspect) throws JOSEException, ParseException {
		String token = introspect.getToken();
		boolean isValid = true;
		try {
			jwtUtils.verifyToken(token);
		} catch (HandleException e) {
			isValid = false;
		}

		return IntrospectResponse.builder().valid(isValid).build();
	}

	@Override
	@Transactional
	public ResponseEntity<StateResponse<Object>> refreshToken(HttpServletResponse response, String token)
			throws JOSEException, ParseException {
		SignedJWT jwtSignedJWT = jwtUtils.verifyToken(token);

		String jit = jwtSignedJWT.getJWTClaimsSet().getJWTID();
		Date expiryTime = jwtSignedJWT.getJWTClaimsSet().getExpirationTime();

		InvalidatedToken invalidatedToken = InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();

		invalidatedTokenRepository.save(invalidatedToken);

		String username = jwtSignedJWT.getJWTClaimsSet().getSubject();

		User user = repository.findByUserName(username)
				.orElseThrow(() -> new HandleException(ErrorCode.USER_NOT_EXISTED));

		String accessToken = jwtUtils.generateToken(user, false);

		ResponseCookie accessCookie = jwtUtils.generateAccessCookie(accessToken);
		ResponseCookie refreshCookie = jwtUtils.generateRefreshCookie(token);

		int reputation = 0;
		String tier = "NONE";
		if (user instanceof NormalUser normalUser) {
			reputation = normalUser.getReputationScore();
			tier = (normalUser.getCurrentTier() != null) ? normalUser.getCurrentTier().getId() : "NONE";
		} else if (user instanceof Admin) {
			tier = "ADMIN";
		}

		ResponseEntity<StateResponse<Object>> responseEntity = ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, accessCookie.toString())
				.header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
				.body(StateResponse.builder()
						.result(AuthenticationResponse.builder()
								.auth(true)
								.build())
						.build());

		return responseEntity;

	}

	@Override
	public void logout(HttpServletResponse response, String token) throws JOSEException, ParseException {

		jwtUtils.clearToken(response);

		SignedJWT signToken = jwtUtils.verifyToken(token);

		String jit = signToken.getJWTClaimsSet().getJWTID();
		Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

		InvalidatedToken invalidatedToken = InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();

		invalidatedTokenRepository.save(invalidatedToken);

	}

}
