package com.sist.web.service;

import java.util.List;
import java.util.Map;

import com.sist.web.vo.*;

// 부탁받을 내용
public interface RecipeService {


	public List<RecipeListVO> recipeListData(Map map);
    
	// 내부에서 총페이지 호출하는 방식으로 
	public int[] pages(int page);
	
	public List<RecipeListVO> recipeCategoryData(Map map);
	
	// 카테고리별 페이지 블록 계산 
	 public int[] category_pages (Map map); 
	 
	// 좋아요 토글 (true=좋아요 등록됨, false=좋아요 취소됨)
	 public boolean toggleLike(Map map);
	 
	 
	// 레시피 키워드 검색
	public List<RecipeListVO> recipeSearchData(Map map);
	
	// 키워드 검색 총 페이지 계산식 관련
	public int[] search_pages(Map map);
	
	// 레시피 총 개수
	public int recipeTotalCount();
	
	// 카테고리 + 키워드 통합 조회
	public List<RecipeListVO> recipeFilterData(Map map);

	// 카테고리 + 키워드 통합 조회용 페이지 블록 계산 (내부에서 filterTotalPage 호출)
	public int[] filter_pages(Map map);
	
	// 키워드 검색 결과 총 개수
	public int searchTotalCount(Map map);

	// 카테고리+키워드 통합 조회 결과 총 개수
	public int filterTotalCount(Map map);
}
