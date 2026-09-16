package com.sist.web.vo;

import java.util.List;

import lombok.Data;
@Data
public class FillResultVO {
	private int filledCount;           // 몇 개 채웠는지
    private List<FilledSlotVO> filledSlots; // 채워진 슬롯들 목록
}
