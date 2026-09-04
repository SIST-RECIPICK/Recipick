package com.sist.web.vo;

import java.util.Date;

import lombok.Data;

/*
 * ID            NOT NULL NUMBER 
USERS_ID      NOT NULL NUMBER 
INGREDIENT_ID NOT NULL NUMBER 
CREATED_AT             DATE   

 */
@Data
public class RefridgeVO {
    private int id;
    private int users_id;
    private int ingredient_id;
    private Date created_at;
    private String dbday;
    
}
