package com.sist.web.vo;

import java.util.Date;

import lombok.Data;

/*
BOOKMARK_ID NOT NULL NUMBER 
USER_ID              NUMBER 
RECIPE_ID            NUMBER 
CREATE_AT            DATE  
 */
@Data
public class RecipeBookMarkVO {
	private int bookmark_id,user_id,recipe_id,dbday;
	private Date create_at;
}
