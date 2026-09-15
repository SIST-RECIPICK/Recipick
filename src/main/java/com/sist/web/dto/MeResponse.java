package com.sist.web.dto;

import lombok.Data;

// [응답: 현재 로그인 사용자 정보 조회]
@Data
public class MeResponse {
	private int userId;
	private String nickname;
	private String role;
}
