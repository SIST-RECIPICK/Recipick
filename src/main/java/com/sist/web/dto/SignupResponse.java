package com.sist.web.dto;

import lombok.Data;

// [응답: 회원가입]
@Data
public class SignupResponse {
	private int userId;
	private String email;
	private String nickname;
}
