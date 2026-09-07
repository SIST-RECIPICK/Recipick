package com.sist.web.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthExceptionHandler {

	// [400 BAD_REQUEST]
	@ExceptionHandler(AuthException.class)
	public ResponseEntity<Map<String, Object>> handleAuthException(AuthException e) {
		Map<String, Object> body = new HashMap<>();
		body.put("errorCode", e.getErrorCode());
		body.put("message", e.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}
}
