package com.sist.web.exception;

import lombok.Getter;

//비지니스 로직 전용 예외처리 
@Getter
public class BusinessException extends RuntimeException{
	private final ErrorCode errorCode;
	
	public BusinessException(ErrorCode errorCode) {
		// RuntimeException의 생성자를 호출할 때 String 메세지를 받고 있으므로 String값을 보내줘야함
		// 나중에 catch 절에서 e.getMessage() 로 값 꺼낼 때 해당 값이 들어감
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}
}
