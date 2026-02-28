package com.example.demo.enums;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

/**
 * Standardized Error Codes for the application.
 * Ranges:
 * 1xxx: System & General Errors
 * 2xxx: Authentication & Security Errors
 * 3xxx: User & Account Errors
 * 4xxx: Resource & Data Errors
 * 5xxx: Feature-Specific (AI/Quiz) Errors
 */
@Getter
public enum ErrorCode {
	// 1xxx: System & General Errors
	UNCATEGORIZED_EXCEPTION(1001, "Lỗi không xác định", HttpStatus.INTERNAL_SERVER_ERROR),
	INVALID_KEY(1002, "Khóa không hợp lệ", HttpStatus.BAD_REQUEST),
	INVALID_METHOD(1003, "Phương thức không hợp lệ", HttpStatus.BAD_REQUEST),
	INTERNAL_SERVER_ERROR(1004, "Lỗi hệ thống nội bộ", HttpStatus.INTERNAL_SERVER_ERROR),

	// 2xxx: Authentication & Security Errors
	UNAUTHENTICATED(2001, "Chưa xác thực", HttpStatus.UNAUTHORIZED),
	UNAUTHORIZED(2002, "Không có quyền truy cập", HttpStatus.FORBIDDEN),
	INVALID_TOKEN(2003, "Token không hợp lệ", HttpStatus.UNAUTHORIZED),
	TOKEN_EXPIRED(2004, "Token đã hết hạn", HttpStatus.UNAUTHORIZED),
	COOKIE_NOT_FOUND(2005, "Không tìm thấy cookie xác thực", HttpStatus.BAD_REQUEST),

	// 3xxx: User & Account Errors
	USER_EXISTED(3001, "Người dùng đã tồn tại", HttpStatus.BAD_REQUEST),
	USER_NOT_EXISTED(3002, "Người dùng không tồn tại", HttpStatus.NOT_FOUND),
	INVALID_USERNAME(3003, "Tên người dùng ít nhất 5 kí tự", HttpStatus.BAD_REQUEST),
	INVALID_PASSWORD(3004, "Mật khẩu ít nhất 8 kí tự", HttpStatus.BAD_REQUEST),
	EMAIL_EXISTED(3005, "Email đã tồn tại", HttpStatus.BAD_REQUEST),
	INVALID_DATE(3006, "Độ tuổi tối thiều là {min}", HttpStatus.BAD_REQUEST),
	PASSWORD_INVALID(3007, "Mật khẩu hiện tại không chính xác", HttpStatus.BAD_REQUEST),

	// 4xxx: Resource & Data Errors
	RESOURCE_NOT_FOUND(4001, "Không tìm thấy tài nguyên", HttpStatus.NOT_FOUND),
	INVALID_RESOURCE(4002, "Dữ liệu nguồn không hợp lệ", HttpStatus.BAD_REQUEST),
	FILE_TOO_LARGE(4003, "Tệp quá lớn", HttpStatus.PAYLOAD_TOO_LARGE),
	INVALID_FILE_TYPE(4004, "Loại tệp không hợp lệ", HttpStatus.BAD_REQUEST),
	DATABASE_ERROR(4005, "Lỗi cơ sở dữ liệu", HttpStatus.INTERNAL_SERVER_ERROR),
	EVALUATED_QUESTIONS(4006, "Câu hỏi đã được đánh giá", HttpStatus.BAD_REQUEST),

	// 5xxx: AI & Quiz Errors
	QUIZ_GENERATION_FAILED(5001, "Tạo bài trắc nghiệm thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
	AI_SERVICE_UNAVAILABLE(5002, "Dịch vụ AI hiện không khả dụng", HttpStatus.SERVICE_UNAVAILABLE),
	;

	private final int code;
	private final String message;
	private final HttpStatusCode httpStatusCode;

	ErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
		this.code = code;
		this.message = message;
		this.httpStatusCode = httpStatusCode;
	}
}
