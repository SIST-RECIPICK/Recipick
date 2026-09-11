package com.sist.web.vo;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class IngredientGroupVO {
	private int ingredient_id;
	private String ingredient_name;
	private int sort_order;
	private List<RecipeVO> recipes = new ArrayList<>();
}
