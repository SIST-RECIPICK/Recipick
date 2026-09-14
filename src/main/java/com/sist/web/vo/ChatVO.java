package com.sist.web.vo;

import java.util.Date;

import lombok.Data;

@Data
public class ChatVO {

    private int room_id,user_id1,user_id2,user_id;
    private String message,nickname;
    private Date create_at;
}
