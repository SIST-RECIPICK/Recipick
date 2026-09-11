package com.sist.web.dto;

import lombok.Data;

// [요청: 회원탈퇴(소프트)]
@Data
public class WithdrawRequest {
	private String password;
}
