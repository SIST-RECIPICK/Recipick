package com.sist.web.dto;

import lombok.Data;

// [응답: 이메일 중복 확인]
@Data
public class EmailCheckResponse {
	private boolean available; // 사용 가능 여부
	private String reason;	   // 사용 불가 시 사유 코드
}
