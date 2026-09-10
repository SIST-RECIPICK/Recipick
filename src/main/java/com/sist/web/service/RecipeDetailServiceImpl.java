package com.sist.web.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sist.web.mapper.RecipeDetailMapper;
import com.sist.web.vo.IngredientUnitVO;
import com.sist.web.vo.MyListVO;
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

	@Override
	public List<MyListVO> userLikeList(int user_id, int start) {
		
		start = (start*12)-12;
		
		return mapper.userLikeList(user_id, start);
	}

	@Override
	public int userLikeListCount(int user_id) {
		
		return mapper.userLikeListCount(user_id);
	}

	@Override
	public int[] pages(int user_id, int page) {
		
		int curpage = page;
		int totalpage =  (int)(Math.ceil(userLikeListCount(user_id)/12.0));
		int startPage = ((page/5)*5)+1;
		int endPage = ((page/5)*5)*5;
		if(endPage > totalpage)
		{
			endPage = totalpage;
		}
		
		int[] pages = {curpage,totalpage,startPage,endPage};
		
		return pages;
	}

	@Override
	public List<MyListVO> userMarkList(int user_id, int start) {
	
		start = (start*12)-12;
		
		return mapper.userMarkList(user_id, start);
	}

	@Override
	public int userMarkListCount(int user_id) {
		
		return mapper.userMarkListCount(user_id);
	}
}
