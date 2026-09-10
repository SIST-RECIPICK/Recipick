package com.sist.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

// [응답: 토큰 재발급]
@Data
public class ReissueResponse {
	private String accessToken;

	// Refresh Token은 응답 body에 노출하지 않고 Controller가 쿠키로 내려보낼 때만 사용
	@JsonIgnore
	private String refreshToken;
}
