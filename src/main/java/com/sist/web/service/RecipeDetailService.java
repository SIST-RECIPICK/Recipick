package com.sist.web.service;

import java.util.List;

import com.sist.web.vo.IngredientUnitVO;
import com.sist.web.vo.MyListVO;
import com.sist.web.vo.RecipeManualVO;
import com.sist.web.vo.RecipeVO;

public interface RecipeDetailService {

	public RecipeVO recipeDetailData(int rcp_seq);
	public List<RecipeManualVO> recipeHowList(int rcp_seq);
	public List<IngredientUnitVO> ingredientUnitList(int rcp_seq);
	public int recipeDetailLikeExist(int recipe_id,int user_id);
	public void recipeDetailLikeInsert(int recipe_id,int user_id);
	public void recipeDetailLikeDelete(int recipe_id,int user_id);
	public int recipeDetailBookmarkExist(int recipe_id, int user_id);
	public void recipeDetailBookmarkInsert(int recipe_id,int user_id);
	public void recipeDetailBookmarkDelete(int recipe_id,int user_id);
	public List<MyListVO> userLikeList(int user_id,int start);
	public int userLikeListCount(int user_id);
	public List<MyListVO> userMarkList(int user_id,int start);
	public int userMarkListCount(int user_id);
	public int[] pages(int user_id, int page);
}
