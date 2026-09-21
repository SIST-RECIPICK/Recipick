package com.sist.web.service;

import java.util.List;
import java.util.Map;

import com.sist.web.vo.RecipeVO;
import com.sist.web.vo.RefridgeVO;

public interface RefridgeService {
	public void registerData(List<RefridgeVO> volist);
	
	public List<RefridgeVO> fridgeData(int user_id);
	
	
	public List<Map<String, Object>> recommandRecipe(List<String> ingredients,String sort);
	
	//public RecipeVO oracleRecipeAllData(int rcp_seq);
	
	public void deleteByUserId(int users_id);
}
