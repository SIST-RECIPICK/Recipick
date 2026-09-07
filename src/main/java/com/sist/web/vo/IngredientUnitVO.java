package com.sist.web.vo;

import lombok.Data;

@Data
public class IngredientUnitVO {
	private int recipe_id;
    private String name;
    private Double amount;
    private String unit;
    private String amount_text;
    private String original;
}
