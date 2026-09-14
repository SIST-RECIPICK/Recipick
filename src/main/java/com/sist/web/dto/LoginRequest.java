package com.sist.web.dto;

import lombok.Data;

// [요청: 로그인]
@Data
public class LoginRequest {
	private String email;
	private String password;
}
