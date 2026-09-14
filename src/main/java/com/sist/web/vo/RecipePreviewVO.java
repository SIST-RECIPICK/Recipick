package com.sist.web.vo;

import java.util.*;

import lombok.Data;
@Data
public class RecipePreviewVO {
	private int rcp_seq;
	private String rcp_nm,hash_tag;
	private double info_eng,info_pro;
	private List<String> ingredients;
}
