package com.sist.web.service;

import org.springframework.stereotype.Service;

import com.sist.web.mapper.RecipeDetailMapper;
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
	
	
}
