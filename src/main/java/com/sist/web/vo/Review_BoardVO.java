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
}
