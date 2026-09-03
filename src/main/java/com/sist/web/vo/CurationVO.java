package com.sist.web.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CurationVO {
	private int id;  
	private int rn; // 화면 번호
	private String title;     
	private int year;      
	private int month;
	private String targetday;
	private String status;    
	private String incredient_list;
	private LocalDateTime created_at;
	private int users_id;  
}
