package com.sist.web.dto;

import lombok.Data;

// [요청: 계정 복구]
@Data
public class RecoverRequest {
	private String recoveryToken;
}
