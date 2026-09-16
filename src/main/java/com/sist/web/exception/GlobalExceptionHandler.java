package com.sist.web.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	// [AuthException.status에 담긴 상태코드로 응답 (기본값 400 BAD_REQUEST)]
	@ExceptionHandler(AuthException.class)
	public ResponseEntity<Map<String, Object>> handleAuthException(AuthException e) {
		Map<String, Object> body = new HashMap<>();
		body.put("errorCode", e.getErrorCode());
		body.put("message", e.getMessage());
		return ResponseEntity.status(e.getStatus()).body(body);
	}
	
	// 비지니스 로직 로깅
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<Map<String, Object>> handleBussinessException(BusinessException e) {
		ErrorCode ec = e.getErrorCode();
	    Map<String, Object> body = new HashMap<>();
	    body.put("errorCode", ec.name()); // Enum으로 작성한 에러정의명이 나옴 ex. RECIPE_NOT_FOUND
	    body.put("message", e.getMessage()); // ex. 레시피를 찾을 수 없습니다.
	    return ResponseEntity.status(ec.getHttpStatus()).body(body);
	}
	
	// 전역 로깅
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> handleException(Exception e) {
		log.error("처리되지 않은 예외 발생", e);
	    ErrorCode ec = ErrorCode.INTERNAL_ERROR; 
	    Map<String, Object> body = new HashMap<>();
	    body.put("errorCode", ec.name());
	    body.put("message", ec.getMessage()); // 내부 정보 유출을 막기 위해 설정해둔 에러 메세지 보여줌
	    return ResponseEntity.status(ec.getHttpStatus()).body(body);
	}
}
