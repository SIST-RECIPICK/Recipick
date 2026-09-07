package com.sist.web.vo;

import lombok.Data;

/*
RCP_SEQ     NOT NULL NUMBER(10)     
STEP_NO     NOT NULL NUMBER(3)      
MANUAL_DESC          VARCHAR2(1000) 
MANUAL_IMG           VARCHAR2(500) 
 */
@Data
public class RecipeManualVO {
	private int rcp_seq,step_no;
	private String manual_desc,manual_img;
	
}
