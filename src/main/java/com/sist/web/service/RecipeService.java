package com.sist.web.service;

import java.util.List;

import com.sist.web.vo.*;

public interface RecipeService {

	public List<RecipeListVO> recipeListData(int start);

	public int[] pages(int page);
}
