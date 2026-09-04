package com.sist.web.vo;

import lombok.Data;

/*
 *  ID              NOT NULL NUMBER        
	CATEGORY_NAME   NOT NULL VARCHAR2(100) 
	INGREDIENT_NAME NOT NULL VARCHAR2(100)
 * 
 */
@Data
public class IngredientVO {
	private int id;
	private String category_name, ingredient_name;
}
