package com.sist.web.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	// 레시피
	RECIPE_NOT_FOUND(HttpStatus.NOT_FOUND, "레시피를 찾을 수 없습니다."),
	
	// 큐레이션
	CURATION_NOT_FOUND(HttpStatus.NOT_FOUND, "큐레이션을 찾을 수 없습니다."),
	
	// AI
	AI_IN_PROGRESS(HttpStatus.CONFLICT, "추천을 생성 중입니다. 잠시만 기다려주세요."),
	AI_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "추천 생성에 실패했습니다. 잠시 후 다시 시도해주세요."),
	
	// 공통
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
	
	
	private final HttpStatus httpStatus;
	private final String message;
		
}
