package com.sist.web.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sist.web.mapper.RecipeDetailMapper;
import com.sist.web.vo.IngredientUnitVO;
import com.sist.web.vo.RecipeManualVO;
import com.sist.web.vo.RecipeVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecipeDetailServiceImpl implements RecipeDetailService{
	private final RecipeDetailMapper mapper;

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
		
		return mapper.ingredientUnitList(rcp_seq);
	}
	
	
}
