package com.sist.web.vo;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonFormat;

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
	private int id,user_id,rcp_seq;
	private String meal_type,rcp_nm,att_file_no_main;
	@JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
	private Date meal_date;
}
