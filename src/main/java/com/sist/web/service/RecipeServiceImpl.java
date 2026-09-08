package com.sist.web.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sist.web.mapper.RecipeMapper;
import com.sist.web.vo.RecipeListVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecipeServiceImpl implements RecipeService {

	private final RecipeMapper rMapper;

	@Override
	public List<RecipeListVO> recipeListData(int start) {
		// TODO Auto-generated method stub
		return rMapper.recipeListData(start);
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

}
