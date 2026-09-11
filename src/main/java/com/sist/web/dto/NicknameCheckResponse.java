package com.sist.web.dto;

import lombok.Data;

// [응답: 닉네임 중복 확인]
@Data
public class NicknameCheckResponse {
	private boolean available; // 사용 가능 여부
}
