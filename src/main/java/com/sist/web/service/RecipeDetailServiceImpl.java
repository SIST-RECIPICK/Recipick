package com.sist.web.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sist.web.mapper.RecipeDetailMapper;
import com.sist.web.vo.IngredientUnitVO;
import com.sist.web.vo.RecipeLikeVO;
import com.sist.web.vo.RecipeManualVO;
import com.sist.web.vo.RecipeVO;
import com.sist.web.vo.ShopLinkVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecipeDetailServiceImpl implements RecipeDetailService{
	private final RecipeDetailMapper mapper;
	private final ShopLinkService shopService;

	@Override
	public RecipeVO recipeDetailData(int rcp_seq) {
	
		return mapper.recipeDetailData(rcp_seq);
	}

	@Override
	public List<RecipeManualVO> recipeHowList(int rcp_seq) {
		
		return mapper.recipeHowList(rcp_seq);
	}

	@Override
	public List<IngredientUnitVO> ingredientUnitList(int rcp_seq) {
		
		List<IngredientUnitVO> unitList = mapper.ingredientUnitList(rcp_seq);
		
		for(IngredientUnitVO vo : unitList)
		{
			List<ShopLinkVO> linkVO = shopService.shopLinkData(vo.getName());

			vo.setShopVO(linkVO);
		}
	
		return unitList;
	}

	@Override
	public int recipeDetailLikeExist(int rcp_seq,int user_id) {
	
		return mapper.recipeDetailLikeExist(rcp_seq,user_id);
	}

	@Override
	public void recipeDetailLikeInsert(int rcp_seq,int user_id) {
		
		mapper.recipeDetailLikeInsert(rcp_seq,user_id);
	}

	@Override
	public void recipeDetailLikeDelete(int rcp_seq,int user_id) {
		
		mapper.recipeDetailLikeDelete(rcp_seq,user_id);
	}

	@Override
	public int recipeDetailBookmarkExist(int recipe_id, int user_id) {
		
		return mapper.recipeDetailBookmarkExist(recipe_id, user_id);
	}

	@Override
	public void recipeDetailBookmarkInsert(int recipe_id, int user_id) {
		
		mapper.recipeDetailBookmarkInsert(recipe_id, user_id);
	}

	@Override
	public void recipeDetailBookmarkDelete(int recipe_id, int user_id) {
		
		mapper.recipeDetailBookmarkDelete(recipe_id, user_id);
	}
}
