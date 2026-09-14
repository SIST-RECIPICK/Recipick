package com.sist.web.service;

import java.util.List;
import java.util.Map;

import com.sist.web.vo.CurationCreateVO;
import com.sist.web.vo.CurationVO;
import com.sist.web.vo.RecipeVO;
import com.sist.web.vo.UsersVO;

public interface AdminService {
	public List<UsersVO> usersList(int page);
	
	public int[] pages(int page, String tablename);
	
	public void userRoleUpdate(int id, String role);
	
	public void userStatusUpdate(int id, String status);
	
	public List<CurationVO> curation_list(int page);
	
	public CurationVO selectCurationDetail(int id);

	public void deleteCuration(int id);

	public Map<String, List<RecipeVO>> selectRecipeTop3(List<Integer> ids);

	public void insertCuration(CurationCreateVO vo);

	public void updateCuration(CurationCreateVO vo, int id);
}
