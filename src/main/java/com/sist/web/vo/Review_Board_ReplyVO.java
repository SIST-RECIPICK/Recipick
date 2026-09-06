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
    
    // 댓글 작성자(user)
    private String writer_nickname;
}
