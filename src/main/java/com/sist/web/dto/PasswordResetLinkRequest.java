package com.sist.web.dto;

import lombok.Data;

// [요청: 비밀번호 재설정 링크 요청]
@Data
public class PasswordResetLinkRequest {
	private String email;
}
