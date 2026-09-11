package com.sist.web.vo;

import lombok.Data;

@Data
public class CurationDetailVO {
	private int id;           
	private int sort_order;   
	private int curation_id;  
	private int ingredient_id;
	private String ingredient_name;
	private int rcp_seq;      
	private String rcp_nm;
	private String att_file_no_main;
	private int hit;
}