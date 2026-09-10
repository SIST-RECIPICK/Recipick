package com.sist.web.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.sist.web.mapper.RecipeMapper;
import com.sist.web.vo.RecipeListVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
// 서비스에서 부탁 받은 내용을 실행하는 곳
public class RecipeServiceImpl implements RecipeService {

	private final RecipeMapper rMapper;

	@Override
	public List<RecipeListVO> recipeListData(Map map) {
		// TODO Auto-generated method stub
		return rMapper.recipeListData(map);
	}

	@Override
	public int[] pages(int page) {

		int totalpage = rMapper.recipeTotalPage();
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
	public List<RecipeListVO> recipeCategoryData(Map map) {
		// TODO Auto-generated method stub
		return rMapper.recipeCategoryData(map);
	}

	// 카테고리 총페이지 구하기
	@Override
	public int[] category_pages(Map map) {
		// TODO Auto-generated method stub
		
		// 이 카테고리(예: 반찬)에 레시피가 총 몇 개 있고, 그걸 12개씩 나누면 총 몇 페이지가 되는지, 그 숫자 하나를 DB에서 가져오는 것
		// 총 49 페이지면 49 그 숫자를 가져오는 것
		int c_totalpage = rMapper.categoryTotalPage((String) map.get("main_category"));
		int page = (Integer) map.get("page"); // "c_page가 컨트롤러 실제값에 넣어야 하는 형태"
		final int BLOCK = 10;
		int startpage = ((page - 1) / BLOCK * BLOCK) + 1;
		int endpage = ((page - 1) / BLOCK * BLOCK) + BLOCK;
		if (endpage >c_totalpage)
			endpage = c_totalpage;
		int[] pages = { page, c_totalpage, startpage, endpage };
		
		return pages;
	}

	// 좋아요 토글 버튼
	// Map => user_id(좋아요 누른 사용자), recipe_id(좋아요 누른 레시피)
	@Override
	public boolean toggleLike(Map map) {
	    int count = rMapper.checkRecipeLike(map);
	    if (count > 0) {
	        // 이미 눌렀음 -> 취소
	        rMapper.deleteRecipeLike(map);
	        return false;
	    } else {
	        // 안 눌렀음 -> 등록
	        rMapper.insertRecipeLike(map);
	        return true;
	    }
	}
}
