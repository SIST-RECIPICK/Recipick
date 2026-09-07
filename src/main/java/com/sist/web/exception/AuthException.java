package com.sist.web.exception;

import lombok.Getter;

// [예외: 인증 처리 전용]
@Getter
public class AuthException extends RuntimeException {
	private final String errorCode;

	public AuthException(String errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}
}
