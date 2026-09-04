package com.sist.web.service;

import java.util.List;

import com.sist.web.vo.CurationDetailVO;
import com.sist.web.vo.CurationVO;
import com.sist.web.vo.UsersVO;

public interface AdminService {
	public List<UsersVO> usersList(int page);
	
	public int[] pages(int page, String tablename);
	
	public void userRoleUpdate(int id, String role);
	
	public void userStatusUpdate(int id, String status);
	
	public List<CurationVO> curation_list(int page);
	
	public CurationVO selectCurationDetail(int id);
}
