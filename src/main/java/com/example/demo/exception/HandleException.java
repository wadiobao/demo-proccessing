
package com.example.demo.exception;

import com.example.demo.enums.ErrorCode;

public class HandleException extends RuntimeException {

	private ErrorCode errorCode;

	public HandleException(ErrorCode code) {
		super();
		this.errorCode = code;
	}

	public ErrorCode getErrorCode() {
		return errorCode;
	}

	public void setErrorCod(ErrorCode code) {
		this.errorCode = code;
	}
	
}

