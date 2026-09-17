package com.sist.web.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sist.web.mapper.RecipeMapper;
import com.sist.web.vo.IngredientUnitInsertVO;
import com.sist.web.vo.RecipeInsertVO;
import com.sist.web.vo.RecipeListVO;
import com.sist.web.vo.RecipeManualVO;

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

	// 레시피 키워드 검색
	@Override
	public List<RecipeListVO> recipeSearchData(Map map) {
		return rMapper.recipeSearchData(map);
	}

	// 키워드 검색 총페이지 구하기
	@Override
	public int[] search_pages(Map map) {

		int s_totalpage = rMapper.searchTotalPage(map);
		int page = (Integer) map.get("page");
		final int BLOCK = 10;
		int startpage = ((page - 1) / BLOCK * BLOCK) + 1;
		int endpage = ((page - 1) / BLOCK * BLOCK) + BLOCK;
		if (endpage > s_totalpage)
			endpage = s_totalpage;
		int[] pages = { page, s_totalpage, startpage, endpage };

		return pages;
	}

	// 총 레시피 개수
	@Override
	public int recipeTotalCount() {
		// TODO Auto-generated method stub
		return rMapper.recipeTotalCount();
	}

	// 카테고리 + 키워드 통합 조회
	@Override
	public List<RecipeListVO> recipeFilterData(Map map) {
		return rMapper.recipeFilterData(map);
	}

	// 카테고리 + 키워드 통합 조회 페이지 블록 계산
	@Override
	public int[] filter_pages(Map map) {

		int f_totalpage = rMapper.filterTotalPage(map);
		int page = (Integer) map.get("page");
		final int BLOCK = 10;
		int startpage = ((page - 1) / BLOCK * BLOCK) + 1;
		int endpage = ((page - 1) / BLOCK * BLOCK) + BLOCK;
		if (endpage > f_totalpage)
			endpage = f_totalpage;
		int[] pages = { page, f_totalpage, startpage, endpage };

		return pages;
	}	
	
	// 키워드 검색 결과 총 개수
	@Override
	public int searchTotalCount(Map map) {
		return rMapper.searchTotalCount(map);
	}

	// 카테고리+키워드 통합 조회 결과 총 개수
	@Override
	public int filterTotalCount(Map map) {
		return rMapper.filterTotalCount(map);
	}

	// 나의 레시피 목록 조회
	@Override
	public List<RecipeListVO> myRecipeListData(Map map) {
		// TODO Auto-generated method stub
		return rMapper.myRecipeListData(map);
	}

	// 나의 레시피 총페이지 수 구하기
	@Override
	public int myTotalPage(Map map) {
	    return rMapper.myTotalPage(map);
	}

	// 나의 레시피 페이지 블록 계산
	@Override
	public int[] myPages(Map map) {

	    int my_totalpage = rMapper.myTotalPage(map);
	    int page = (Integer) map.get("page");
	    final int BLOCK = 10;
	    int startpage = ((page - 1) / BLOCK * BLOCK) + 1;
	    int endpage = ((page - 1) / BLOCK * BLOCK) + BLOCK;
	    if (endpage > my_totalpage)
	        endpage = my_totalpage;
	    int[] pages = { page, my_totalpage, startpage, endpage };

	    return pages;
	}

	// 나의 레시피 총 개수 구하기
	@Override
	public int myTotalCount(Map map) {
	    return rMapper.myTotalCount(map);
	}

	// 레시피 등록
	// 1) RECIPE 테이블에 제목/재료/칼로리 같은 기본정보 1줄 저장
    //  -> 저장되면서 vo(등록하려는 레시피 정보)의 rcp_seq에 새 번호가 자동으로 채워짐
	// 2) 그 번호를 조리순서 하나하나에 넣어서, RECIPE_MANUA 테이블에 여러 줄 저장
	@Override
	public int recipeInsert(RecipeInsertVO vo) {

		// 1. 레시피 기본정보 저장
		rMapper.recipeInsert(vo);

		// 2. 조리순서 리스트를 하나씩 꺼내서, 방금 생긴 레시피 번호를 넣어 저장
		List<RecipeManualVO> manualList = vo.getManualList();  // manualList: 조리순서 여러 단계를 담은 리스트
		for (RecipeManualVO manual : manualList) {  // manual: 조리순서 "한 단계" (1단계, 2단계...)

			// manual(조리순서 한 단계)의 rcp_seq 자리에,
			// 방금 새로 생성된 레시피 번호(vo.getRcp_seq())를 넣어줌
			// -> 이렇게 해야 이 조리순서가 "몇 번 레시피 것인지" DB에 같이 저장됨
			manual.setRcp_seq(vo.getRcp_seq());

			rMapper.recipeManualInsert(manual);
		}
		// 재료 리스트를 하나씩 꺼내서, 방금 생긴 레시피 번호를 넣어 저장
				for(IngredientUnitInsertVO ingredient : vo.getIngredientList())
				{
					ingredient.setRecipe_id(vo.getRcp_seq());
					rMapper.ingredientInsert(ingredient);
				}
				
			

		// 3. 새로 생성된 레시피 번호를 컨트롤러에 돌려줌
		return vo.getRcp_seq();
	}

	// 레시피 삭제
/*
 * recipe 테이블은 recipe_like, recipe_bookmark, recipe_manual이 FK로 참조 중
 * FK에 CASCADE가 안 걸려있으므로(특히 like/bookmark는 공용 테이블이라 구조 변경 불가),
 * 자식 데이터를 먼저 지우고 마지막에 부모(recipe)를 지우는 순서로 처리
 *
 * @Transactional: 아래 4개의 DELETE를 하나의 트랜잭션으로 묶음
 *   -> 중간에 하나라도 실패하면 앞서 지워진 것들도 전부 롤백(원상복구)됨
 *   -> 일부만 지워진 상태로 어중간하게 남는 것을 방지
 */

	@Override
	@Transactional
	public int deleteRecipe(int rcp_seq) {

	    // 1. 좋아요 기록 삭제 (자식)
	    rMapper.deleteRecipeLike(rcp_seq);

	    // 2. 북마크 기록 삭제 (자식)
	    rMapper.deleteRecipeBookmark(rcp_seq);

	    // 3. 조리순서 삭제 (자식)
	    rMapper.deleteRecipeManual(rcp_seq);

	    // 재료 정보 삭제
	    rMapper.deleteIngredientUnit(rcp_seq);
	    
	    // 4. 마지막으로 레시피 원본 삭제 (부모)
	    // 자식이 모두 정리된 상태이므로 FK 제약조건 위반 없이 정상 삭제됨
	    return rMapper.deleteRecipe(rcp_seq);
	}

	// 레시피 수정
	/*
	 * recipe(본문)와 recipe_manual(조리순서) 두 테이블을 같이 건드리므로
	 * @Transactional로 묶음
	 *   -> 본문 수정은 됐는데 조리순서 처리 중 하나라도 실패하면
	 *      전부 롤백(원상복구)되도록 하기 위함
	 */
	@Override
	@Transactional
	public void recipeUpdate(RecipeInsertVO vo) throws Exception {

		// 1. 레시피 본문(제목/재료/칼로리 등) 수정
		rMapper.recipeUpdate(vo);

		// 2. 기존 조리순서는 전부 지움
		//    (단계 개수가 늘거나 줄 수 있어서, 하나씩 비교 수정 대신 통째로 지우고 다시 넣는 방식)
		rMapper.recipeManualDeleteByRcpSeq(vo.getRcp_seq());

		// 3. 새로 받은 조리순서 리스트를 다시 하나씩 저장
		List<RecipeManualVO> manualList = vo.getManualList();
		for (RecipeManualVO manual : manualList) {

			// manual(조리순서 한 단계)에 rcp_seq를 다시 넣어줌
			// -> 이 조리순서가 "몇 번 레시피 것인지" 연결하기 위함
			manual.setRcp_seq(vo.getRcp_seq());

			rMapper.recipeManualInsert(manual);
		}
	}

	// 레시피 재료 정보 저장
	@Override
	public void ingredientInsert(IngredientUnitInsertVO vo) {
		// TODO Auto-generated method stub
		// 재료정보 저장 => 한 줄 디비에 저
		rMapper.ingredientInsert(vo);
		
		
	}

	// 레시피 수정 시 이미지 수정 안 할 경우 기존 이미지 유지
	@Override
	public List<RecipeManualVO> manualImageUpdate(int rcp_seq) {
		// TODO Auto-generated method stub
		return rMapper.manualImageUpdate(rcp_seq);
		
	}

	// 레시피 삭제 시 재료정보 삭제
	@Override
	public int deleteIngredientUnit(int rcp_seq) {
		// TODO Auto-generated method stub
		return rMapper.deleteIngredientUnit(rcp_seq);
	}

	//  특정 카테고리 총 레시피 개수 
	@Override
	public int categoryTotalRecipe(String main_category) {
		// TODO Auto-generated method stub
		return rMapper.categoryTotalPage(main_category);
	}

	
}
