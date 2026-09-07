package com.sist.web.service;

import java.util.List;

import com.sist.web.vo.IngredientUnitVO;
import com.sist.web.vo.RecipeManualVO;
import com.sist.web.vo.RecipeVO;

public interface RecipeDetailService {

	public RecipeVO recipeDetailData(int rcp_seq);
	public List<RecipeManualVO> recipeHowList(int rcp_seq);
	public List<IngredientUnitVO> ingredientUnitList(int rcp_seq);
}
