package com.sist.web.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

// [예외: 인증 처리 전용]
@Getter
public class AuthException extends RuntimeException {
	private final String errorCode;
	private final HttpStatus status;

	public AuthException(String errorCode, String message) {
		this(errorCode, message, HttpStatus.BAD_REQUEST);
	}

	public AuthException(String errorCode, String message, HttpStatus status) {
		super(message);
		this.errorCode = errorCode;
		this.status = status;
	}
}
