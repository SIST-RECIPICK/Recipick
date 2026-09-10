package com.sist.web.service;

import java.util.List;

import com.sist.web.vo.RecipePreviewVO;
import com.sist.web.vo.RecipeSearchVO;

public interface RecipeSearchService {
	public List<RecipeSearchVO> selectRecipeSearch(String keyword,int user_id);
	public RecipePreviewVO  selectRecipePreview(int rcp_seq);
}
