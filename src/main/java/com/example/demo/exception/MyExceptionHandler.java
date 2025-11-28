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
import com.example.demo.sql.entity.User;

import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class MyExceptionHandler {
	
	private static final String MIN_ATTRIBUTE = "min";


	@ExceptionHandler(value = RuntimeException.class)
	ResponseEntity<StateResponse<User>> handlingRuntimeException(RuntimeException exception) {
		StateResponse<User> response = new StateResponse<User>();
		response.setMessage(exception.getMessage());
		return ResponseEntity.badRequest().body(response);
	}

	@ExceptionHandler(value = HandleException.class)
	ResponseEntity<StateResponse> handlingException(HandleException exception) {
		StateResponse response = new StateResponse();
		ErrorCode code = exception.getErrorCode();
		response.setCode(code.getCode());
		response.setMessage(code.getMessage());
		return ResponseEntity.status(code.getHttpStatusCode()).body(response);
	}

	@ExceptionHandler(value = MethodArgumentNotValidException.class)
	ResponseEntity<StateResponse<User>> handlingMethodArgumentNotValidException(
			MethodArgumentNotValidException exception) {
		String enumKey = exception.getFieldError().getDefaultMessage();
		
		
		ErrorCode code = ErrorCode.INVALID_METHOD;
		Map<String,Object> attributes = null;
		
		try {
			code = ErrorCode.valueOf(enumKey);
			
			//Lấy dữ liệu từ annotation
			var constraintViolation = exception.getBindingResult().getAllErrors().get(0).unwrap(ConstraintViolation.class);
			
			attributes = constraintViolation.getConstraintDescriptor().getAttributes();
			
			log.info(attributes.toString());
		}catch(IllegalArgumentException argumentException) {
			
		}
		
		StateResponse<User> response = new StateResponse<User>();

		response.setCode(code.getCode());
		response.setMessage(Objects.nonNull(attributes) ? mapAttribute(code.getMessage(), attributes) : code.getMessage());
		return ResponseEntity.badRequest().body(response);
	}

	@ExceptionHandler(value = AccessDeniedException.class)
	ResponseEntity<StateResponse> handlingAccessDeniedException(AccessDeniedException accessDeniedException) {
		ErrorCode code = ErrorCode.UNAUTHORIZED;
		return ResponseEntity.status(code.getHttpStatusCode())
				.body(StateResponse.builder().code(code.getCode()).message(code.getMessage()).build());
	}
	
	@ExceptionHandler(value = AuthenticationServiceException.class)
	ResponseEntity<StateResponse> handlingAuthenticationServiceException(AuthenticationServiceException authenticationServiceException){
		ErrorCode code = ErrorCode.UNAUTHORIZED;
		return ResponseEntity.status(code.getHttpStatusCode())
				.body(StateResponse.builder().code(code.getCode()).message(code.getMessage()).build());
	}
	
	@ExceptionHandler(value = NullPointerException.class)
	ResponseEntity<StateResponse> handlingNullPointerException(NullPointerException nullPointerException){
		ErrorCode code = ErrorCode.UNAUTHORIZED;
		return ResponseEntity.status(code.getHttpStatusCode())
				.body(StateResponse.builder().code(code.getCode()).message(code.getMessage()).build());
	}
	
	@ExceptionHandler(value = MissingRequestCookieException.class)
	ResponseEntity<StateResponse> handlingMissingRequestCookieException(MissingRequestCookieException missingRequestCookieException){
		ErrorCode code = ErrorCode.COOKIE_NOT_FOUND;
		return ResponseEntity.status(code.getHttpStatusCode())
				.body(StateResponse.builder().code(code.getCode()).message(code.getMessage()).build());
	}
	
	
	//Hàm map tới message trong enum
	private String mapAttribute(String message, Map<String, Object> attributes) {
        String minValue = String.valueOf(attributes.get(MIN_ATTRIBUTE));

        return message.replace("{" + MIN_ATTRIBUTE + "}", minValue);
    }
}
