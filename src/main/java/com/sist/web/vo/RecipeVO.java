package com.sist.web.vo;

import lombok.Data;

@Data
public class RecipeVO {
	  private int rcp_seq;
    private String rcp_nm;
    private String rcp_way2;
    private String rcp_pat2;
    private String info_wgt;
    private double info_eng;
    private double info_car;
    private double info_pro;
    private double info_fat;
    private double info_na;
    private String hash_tag;
    private String att_file_no_main;
    private String att_file_no_mk;
    private String rcp_parts_dtls;
    private String rcp_na_tip;
    private String user_id;
    private int hit;
}
