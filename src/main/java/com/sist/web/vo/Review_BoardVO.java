package com.sist.web.vo;

import java.util.Date;

import lombok.Data;

@Data
public class Review_BoardVO {
	private int id;
    private int users_id;
    private String subject;
    private String content;
    private Date created_at;
    private int hit;
    private String image_url;
    private double image_size;
    private int rcp_seq;
    
    // 작성자(users)
    private int writer_id;
    private String writer_nickname;
    private String writer_profile_image;

    // 레시피(recipe)
    private String rcp_nm;
    private String att_file_no_main;

    // 쉐프(users)
    private int chef_id;
    private String chef_nickname;
    private String chef_profile_image;
    
}
