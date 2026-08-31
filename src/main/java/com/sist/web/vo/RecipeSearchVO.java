package com.sist.web.vo;

import lombok.Data;

@Data
public class RecipeSearchVO {
	private int rcp_seq;
	private String rcp_nm;
	private String att_file_no_main;
	private double info_eng;
	private String rcp_pat2;
	private int is_bookmark;
}
