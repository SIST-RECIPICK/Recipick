package com.sist.web.restcontroller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sist.web.service.RecipeService;
import com.sist.web.vo.RecipeListVO;
import com.sist.web.vo.RecipeVO;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@CrossOrigin("*")
public class RecipeRestController {

	private final RecipeService rService;

	// 레시피 목록
	@GetMapping("/recipe/list")
	public ResponseEntity<?> recipe_list(@RequestParam("page") int page) {
		Map<String, Object> map = new HashMap<>();
		int start = (page - 1) * 12;
		try {
			List<RecipeListVO> list = rService.recipeListData(start);
			int[] pages = rService.pages(page);
			
			map.put("list", list);
			map.put("pages", pages);
			
		} catch (Exception ex) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		return ResponseEntity.ok(map);
	}

	// 특정카테고리 조회 + 페이징
	@GetMapping("/recipe/category")
	// recipe_category() => 괄호 안에는 파라미터 선언이 들어가야 하는 자리
	// "" 해서 문자열이 들어가면 안 돼
	public ResponseEntity<?> recipe_category(@RequestParam("main_category") String main_category,
			@RequestParam("page") int page)
	{
		// Map<String, Object> 
		// 그냥 클라이언트한테 돌려줄 결과 담는 봉투
		Map<String, Object> resultMap = new HashMap<>(); // 클라이언트 응답용
		Map map = new HashMap(); // 메인카테고리랑, 페이지 담긴 파라미터용 그릇
		
		int start = (page - 1) * 12;
		   
		// 파라미터용 값 채우기
		   map.put("main_category", main_category);
		   map.put("page", page);
		   map.put("start", start);
		
		   // 여기가 클라이언트가 요청했던 값들 채우기
		try {
		    List<RecipeListVO> list = rService.recipeCategoryData(map);
		    int[] pages = rService.category_pages(map);
		    
		    resultMap.put("list", list);
		    resultMap.put("pages", pages);
		    
		} catch (Exception ex) {
		    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		return ResponseEntity.ok(resultMap);
	}
}
