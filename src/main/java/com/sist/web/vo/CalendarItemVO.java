package com.sist.web.vo;
import java.util.*;
import lombok.Data;
/*
	ID        NOT NULL NUMBER       
	USER_ID   NOT NULL NUMBER       
	MEAL_DATE NOT NULL DATE         
	MEAL_TYPE NOT NULL VARCHAR2(50) 
	RCP_SEQ   NOT NULL NUMBER       
 */
@Data
public class CalendarItemVO {
	private int id,user_id,rcp_Seq;
	private String meal_type,rcp_nm,att_file_no_main;
	private Date meal_date;
}
