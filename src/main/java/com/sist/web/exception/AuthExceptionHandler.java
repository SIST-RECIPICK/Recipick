package com.sist.web.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthExceptionHandler {

	// [AuthException.status에 담긴 상태코드로 응답 (기본값 400 BAD_REQUEST)]
	@ExceptionHandler(AuthException.class)
	public ResponseEntity<Map<String, Object>> handleAuthException(AuthException e) {
		Map<String, Object> body = new HashMap<>();
		body.put("errorCode", e.getErrorCode());
		body.put("message", e.getMessage());
		return ResponseEntity.status(e.getStatus()).body(body);
	}
}
