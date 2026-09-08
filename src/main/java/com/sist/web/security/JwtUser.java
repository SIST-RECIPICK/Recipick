package com.sist.web.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

// [@AuthenticationPrincipal로 꺼내는 인증 사용자 정보]
@Getter
@AllArgsConstructor
public class JwtUser {
	private final int userId;
	private final String role;
}
