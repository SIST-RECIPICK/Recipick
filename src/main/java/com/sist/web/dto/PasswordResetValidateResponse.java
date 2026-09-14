package com.sist.web.dto;

import lombok.Data;

// [응답: 비밀번호 재설정 링크 유효성 검증]
@Data
public class PasswordResetValidateResponse {
	private boolean valid;
}
