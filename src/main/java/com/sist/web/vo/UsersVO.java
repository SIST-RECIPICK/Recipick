package com.sist.web.vo;

import java.util.Date;
import lombok.Data;

@Data
public class UsersVO {
	private int id;                
	private String nickname;          
	private String profile_image_url; 
	private String status;            
	private String role;              
	private String introduction;      
	private Date created_at;          
	private Date updated_at;          
	private Date withdrawn_at;        
}
