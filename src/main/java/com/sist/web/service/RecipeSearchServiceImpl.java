package com.sist.web.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sist.web.mapper.RecipeSearchMapper;
import com.sist.web.vo.RecipeSearchVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecipeSearchServiceImpl implements RecipeSearchService{
	private final RecipeSearchMapper rsMapper;
	@Override
	public List<RecipeSearchVO> selectRecipeSearch(String keyword, int user_id) {
		// TODO Auto-generated method stub
		return rsMapper.selectRecipeSearch(keyword,user_id);
	}

}
