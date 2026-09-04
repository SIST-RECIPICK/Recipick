package com.sist.web.vo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

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
	@JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
	private LocalDateTime created_at;
	private int users_id;  
	private List<IngredientGroupVO> group = new ArrayList<>(); 
}
