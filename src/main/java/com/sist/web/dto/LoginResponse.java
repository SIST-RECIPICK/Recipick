package com.sist.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

// [응답: 로그인]
@Data
public class LoginResponse {
	private String accessToken;
	private String accountStatus;
	private Integer userId;
	private String nickname;
	private String role;
	private String recoveryToken;
	private String message;

	// Refresh Token은 응답 body에 노출하지 않고 Controller가 쿠키로 내려보낼 때만 사용
	@JsonIgnore
	private String refreshToken;
}
