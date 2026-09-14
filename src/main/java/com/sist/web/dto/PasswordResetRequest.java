package com.sist.web.dto;

import lombok.Data;

// [요청: 비밀번호 재설정]
@Data
public class PasswordResetRequest {
	private String token;
	private String newPassword;
	private String newPasswordConfirm;
}
