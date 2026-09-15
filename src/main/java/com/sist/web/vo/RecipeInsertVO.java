package com.sist.web.vo;

import java.util.*;


import lombok.Data;

// 레시피 등록
@Data
public class RecipeInsertVO {

	
	private int rcp_seq;            
	private String rcp_nm;          
	private String rcp_way2;         
	private String rcp_pat2;        
	private String info_wgt;         
	private String hash_tag;         
	private String att_file_no_main;
	private String att_file_no_mk;   
	private String rcp_parts_dtls;   
	private String rcp_na_tip;       
	private double info_eng;
	private double info_car;
	private double info_pro;
	private double info_fat;
	private double info_na;
	private int user_id;

	 // 조리순서 => 1단계,2단계 ....
    private List<RecipeManualVO> manualList = new ArrayList<>();
    
    // 재료 정보 여러줄
    private List<IngredientUnitInsertVO> ingredientList = new ArrayList<>();
}