package com.sist.web.service;

import java.util.List;

import com.sist.web.vo.IngredientVO;

public interface IngredientService {

	public List<IngredientVO> searchData(String keyword);
}
