package com.sist.web.service;

import java.util.List;
import java.util.Map;

import com.sist.web.vo.*;

// 부탁받을 내용
public interface RecipeService {

	public List<RecipeListVO> recipeListData(int start);

	public int[] pages(int page);
	
	public List<RecipeListVO> recipeCategoryData(Map map);
	
	// 카테고리별 페이지 블록 계산
	 public int[] category_pages (Map map); 
}
