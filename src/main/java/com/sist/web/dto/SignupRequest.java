package com.sist.web.dto;

import lombok.Data;

// [요청: 회원가입]
@Data
public class SignupRequest {
	private String email;
	private String password;
	private String passwordConfirm;
	private String nickname;
}
