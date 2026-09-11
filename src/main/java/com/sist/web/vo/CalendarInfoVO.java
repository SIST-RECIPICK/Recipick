package com.sist.web.vo;

import lombok.Data;

@Data
public class CalendarInfoVO {
	private int filled_count,total_slots,top1_rcp_seq,top1_count;
	private double total_cal,avg_cal,fill_rate,total_car,total_pro,total_fat;
	private String top1_nm;
}
