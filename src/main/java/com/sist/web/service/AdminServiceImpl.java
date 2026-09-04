package com.sist.web.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sist.web.mapper.AdminMapper;
import com.sist.web.vo.CurationDetailVO;
import com.sist.web.vo.CurationVO;
import com.sist.web.vo.IngredientGroupVO;
import com.sist.web.vo.RecipeVO;
import com.sist.web.vo.UsersVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

	private final AdminMapper adminMapper;
	private final int LIST_SIZE = 15;
	@Override
	public List<UsersVO> usersList(int page) {
		int start = (page - 1) * LIST_SIZE;
		return adminMapper.usersList(start);
	}

	@Override
	public int[] pages(int page, String tablename) {
		if (!tablename.equals("users") && !tablename.equals("curation")) {
	        throw new IllegalArgumentException("허용되지 않은 테이블: " + tablename);
	    }
		int totalpage = adminMapper.totalPageCount(tablename);
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

	@Override
	public List<CurationVO> curation_list(int page) {
		int start = (page - 1) * LIST_SIZE;
		return adminMapper.selectCurationList(start);
	}

	@Override
	public CurationVO selectCurationDetail(int id) {
		
		CurationVO curation = adminMapper.selectCurationHeader(id);
		
		List<CurationDetailVO> detailList = adminMapper.selectCurationDetail(id);
		
		Map<String, IngredientGroupVO> map = new LinkedHashMap<>();
		
		for(CurationDetailVO detail : detailList) {
			String name = detail.getIngredient_name();
			// 처음 나오는 재료라면
			if(!map.containsKey(name)) {
				
				IngredientGroupVO ingredients = new IngredientGroupVO();
				
				ingredients.setIngredient_name(name);
				ingredients.setIngredient_id(detail.getIngredient_id());
				ingredients.setSort_order(detail.getSort_order());
				
				map.put(name, ingredients);
				
			}

			RecipeVO recipe = new RecipeVO();
			recipe.setRcp_seq(detail.getRcp_seq());
			recipe.setRcp_nm(detail.getRcp_nm());
			recipe.setAtt_file_no_main(detail.getAtt_file_no_main());
			recipe.setHit(detail.getHit());
			
			map.get(name).getRecipes().add(recipe);
		}
		
		curation.setGroup(new ArrayList<>(map.values()));
		
		return curation;
	}

	@Override
	@Transactional
	public void deleteCuration(int id) {
		// 상세 데이터 삭제
		adminMapper.deleteCurationDetail(id);
		// 큐레이션 삭제
		adminMapper.deleteCuration(id);
	}

}
