package com.sist.web.vo;

import lombok.Data;

@Data
public class RecipeListVO {

	private int rcp_seq;
	private String rcp_nm;
	private String rcp_pat2;
	private double info_eng;
	private String hash_tag;
	private String att_file_no_main;
	private String user_id;
	private String nickname;
	private int hit;
	private int like_count;
	
}
