package com.example.demo.exception;

import java.util.Map;
import java.util.Objects;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.example.demo.dto.StateResponse;
import com.example.demo.enums.ErrorCode;

import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class MyExceptionHandler {

	private static final String MIN_ATTRIBUTE = "min";

	@ExceptionHandler(value = Exception.class)
	ResponseEntity<StateResponse<Object>> handlingException(Exception exception) {
		log.error("Exception: ", exception);
		StateResponse<Object> response = new StateResponse<>();
		ErrorCode errorCode = ErrorCode.UNCATEGORIZED_EXCEPTION;
		response.setCode(errorCode.getCode());
		response.setMessage(errorCode.getMessage());
		return ResponseEntity.status(errorCode.getHttpStatusCode()).body(response);
	}

	@ExceptionHandler(value = RuntimeException.class)
	ResponseEntity<StateResponse<Object>> handlingRuntimeException(RuntimeException exception) {
		log.error("RuntimeException: ", exception);
		StateResponse<Object> response = new StateResponse<>();
		ErrorCode errorCode = ErrorCode.UNCATEGORIZED_EXCEPTION;
		response.setCode(errorCode.getCode());
		response.setMessage(exception.getMessage() != null ? exception.getMessage() : errorCode.getMessage());
		return ResponseEntity.status(errorCode.getHttpStatusCode()).body(response);
	}

	@ExceptionHandler(value = HandleException.class)
	ResponseEntity<StateResponse<Object>> handlingHandleException(HandleException exception) {
		StateResponse<Object> response = new StateResponse<>();
		ErrorCode code = exception.getErrorCode();
		response.setCode(code.getCode());
		response.setMessage(code.getMessage());
		return ResponseEntity.status(code.getHttpStatusCode()).body(response);
	}

	@ExceptionHandler(value = MethodArgumentNotValidException.class)
	ResponseEntity<StateResponse<Object>> handlingMethodArgumentNotValidException(
			MethodArgumentNotValidException exception) {
		String enumKey = exception.getFieldError().getDefaultMessage();

		ErrorCode code = ErrorCode.INVALID_KEY;
		Map<String, Object> attributes = null;

		try {
			code = ErrorCode.valueOf(enumKey);

			var constraintViolation = exception.getBindingResult().getAllErrors().get(0)
					.unwrap(ConstraintViolation.class);

			attributes = constraintViolation.getConstraintDescriptor().getAttributes();

		} catch (Exception e) {
			log.warn("Could not map validation error key: {}", enumKey);
		}

		StateResponse<Object> response = new StateResponse<>();
		response.setCode(code.getCode());
		response.setMessage(
				Objects.nonNull(attributes) ? mapAttribute(code.getMessage(), attributes) : code.getMessage());

		return ResponseEntity.status(code.getHttpStatusCode()).body(response);
	}

	@ExceptionHandler(value = AccessDeniedException.class)
	ResponseEntity<StateResponse<Object>> handlingAccessDeniedException(AccessDeniedException accessDeniedException) {
		ErrorCode code = ErrorCode.UNAUTHORIZED;
		return ResponseEntity.status(code.getHttpStatusCode())
				.body(StateResponse.builder().code(code.getCode()).message(code.getMessage()).build());
	}

	@ExceptionHandler(value = AuthenticationServiceException.class)
	ResponseEntity<StateResponse<Object>> handlingAuthenticationServiceException(
			AuthenticationServiceException authenticationServiceException) {
		ErrorCode code = ErrorCode.UNAUTHENTICATED;
		return ResponseEntity.status(code.getHttpStatusCode())
				.body(StateResponse.builder().code(code.getCode()).message(code.getMessage()).build());
	}

	@ExceptionHandler(value = MissingRequestCookieException.class)
	ResponseEntity<StateResponse<Object>> handlingMissingRequestCookieException(
			MissingRequestCookieException missingRequestCookieException) {
		ErrorCode code = ErrorCode.COOKIE_NOT_FOUND;
		return ResponseEntity.status(code.getHttpStatusCode())
				.body(StateResponse.builder().code(code.getCode()).message(code.getMessage()).build());
	}

	private String mapAttribute(String message, Map<String, Object> attributes) {
		String minValue = String.valueOf(attributes.get(MIN_ATTRIBUTE));
		return message.replace("{" + MIN_ATTRIBUTE + "}", minValue);
	}
}
