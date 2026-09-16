package com.sist.web.vo;

import lombok.Data;

@Data
public class RollbackResultVO {
	private int rolledBackCount; // 몇 개 되돌렸는지
    private boolean success;
}
