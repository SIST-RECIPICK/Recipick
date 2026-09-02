package com.sist.web.service;

import java.util.List;

import com.sist.web.vo.UsersVO;

public interface AdminService {
	public List<UsersVO> usersList(int page);
	
	public int[] pages(int page);
	
	public void userRoleUpdate(int id, String role);
	
	public void userStatusUpdate(int id, String status);
}
