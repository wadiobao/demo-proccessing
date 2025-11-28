
package com.example.demo.enums;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;


@Getter
public enum ErrorCode {
	INVALID_METHOD(1111,"Invalid",HttpStatus.BAD_REQUEST),
	USER_EXISTED(401,"Người dùng đã tồn tại",HttpStatus.BAD_REQUEST),
	INVALID_PASSWORD(422,"Mật khẩu ít nhất 8 kí tự",HttpStatus.BAD_REQUEST),
	INVALID_USERNAME(422,"Tên người dùng ít nhất 5 kí tự",HttpStatus.BAD_REQUEST),
	USER_NOT_EXISTED(404,"Người dùng không tồn tại",HttpStatus.NOT_FOUND),
	UNAUTHENTICATED(403,"Unauthenticated",HttpStatus.UNAUTHORIZED),
	UNAUTHORIZED(401,"Không có quyền truy cập",HttpStatus.FORBIDDEN),
	INVALID_DATE(422,"Độ tuổi tối thiều là {min}",HttpStatus.BAD_REQUEST),
	COOKIE_NOT_FOUND(404,"Thiếu dữ liệu (c)",HttpStatus.BAD_REQUEST),
	EMAIL_EXISTED(401,"Email đã tồn tại",HttpStatus.BAD_REQUEST),
	INVALID_RESOURCE(422,"Dữ liệu nguồn không hợp lệ",HttpStatus.BAD_REQUEST)
	;
	private int code;
	private String message;
	private HttpStatusCode httpStatusCode;
	
	private ErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
		this.code = code;
		this.message = message;
		this.httpStatusCode =  httpStatusCode;
	}
	
}
