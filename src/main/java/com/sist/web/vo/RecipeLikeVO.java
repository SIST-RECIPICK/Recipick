package com.sist.web.vo;

import java.util.*;

import lombok.Data;
/*
LIKE_ID   NOT NULL NUMBER 
USER_ID            NUMBER 
RECIPE_ID          NUMBER 
CREATE_AT          DATE
*/
@Data
public class RecipeLikeVO {
	private int like_id,user_id,recipe_id,dbday;
	private Date create_at;
	
}
