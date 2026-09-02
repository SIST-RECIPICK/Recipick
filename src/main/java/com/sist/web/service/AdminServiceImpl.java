package com.sist.web.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.sist.web.mapper.AdminMapper;
import com.sist.web.vo.UsersVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

	private final AdminMapper adminMapper;

	@Override
	public List<UsersVO> usersList(int page) {
		int start = (page - 1) * 15;
		return adminMapper.usersList(start);
	}

	@Override
	public int[] pages(int page) {
		int totalpage = adminMapper.userTotalCount();
		// 화면에 몇 개의 페이지를 보여줄 건지 정하는 부분
		final int BLOCK = 10;
		int startpage = ((page - 1) / BLOCK * BLOCK) + 1;
		int endpage = ((page - 1) / BLOCK * BLOCK) + BLOCK;
		if (endpage > totalpage)
			endpage = totalpage;
		int[] pages = { page, totalpage, startpage, endpage };
		return pages;
	}

	@Override
	public void userRoleUpdate(int id, String role) {
		adminMapper.userRoleUpdate(id, role);
	}

	@Override
	public void userStatusUpdate(int id, String status) {
		adminMapper.userStatusUpdate(id, status);
	}

}
