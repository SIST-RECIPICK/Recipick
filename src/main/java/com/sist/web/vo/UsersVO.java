package com.sist.web.vo;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class UsersVO {
	private int id;                
	private String nickname;          
	private String profile_image_url; 
	private String status;            
	private String role;              
	private String introduction;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	private Date created_at;          
	private Date updated_at;          
	private Date withdrawn_at;        
}
