package com.sist.web.vo;

import lombok.Data;

@Data
public class FilledSlotVO {
	private String meal_date;
    private String meal_type;
    private int rcp_seq;
    private String rcp_nm;  // 화면에 이름 보여주려면 필요
}
