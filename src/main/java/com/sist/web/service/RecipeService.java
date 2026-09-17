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
	 
	 // 카테고리 총 레시피 개수
	 public int categoryTotalRecipe (String main_category);
	 
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
	
	// 나의 레시피 목록 조회
	public List<RecipeListVO> myRecipeListData(Map map); 
	
	// 나의 레시피 총페이지 수 구하기
	public int myTotalPage(Map map);
	
	// 나의 레시피 페이지 블록 계산
	public int[] myPages(Map map);
	
	// 나의 레시피 총 개수 구하기
	public int myTotalCount(Map map);
	
	// 레시피 등록 (레시피 본문 1건 + 조리순서 여러 건을 한 번에 저장)
	// 등록 성공하면 새로 만들어진 레시피 번호(rcp_seq)를 돌려줌
	public int recipeInsert(RecipeInsertVO vo);
	
	
	// 레시피 삭제
	// 내부적으로 좋아요/북마크/조리순서까지 함께 정리한 뒤 레시피 원본을 삭제함
	public int deleteRecipe(int rcp_seq);
	
	// 재료정보 삭제
	public int deleteIngredientUnit(int rcp_seq);
	
	// 레시피 수정
	// 내부적으로 recipe 본문 UPDATE + 조리순서(recipe_manual) 삭제 후 재삽입까지 한 번에 처리
	// 이미지 업로드 부분 때문에 예외처리 삽입
	public void recipeUpdate(RecipeInsertVO vo) throws Exception;
	
	// 레시피 등록 시 재료정보 저장
	public void ingredientInsert(IngredientUnitInsertVO vo);
	
	// 레시피 수정 시 이미지 수정 안 할 경우 기존 이미지 유지
	public List<RecipeManualVO> manualImageUpdate(int rcp_seq);
}
