package com.sist.web.vo;

import java.util.Date;

import lombok.Data;

@Data
public class Review_Board_ReplyVO {
	private int id;
    private int review_board_id;
    private int users_id;
    private String content;
    private Date created_at;
}
