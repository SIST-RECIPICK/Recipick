package com.sist.web.restcontroller;

import java.util.*;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sist.web.service.RecipeService;
import com.sist.web.vo.*;


import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@CrossOrigin("*")
public class RecipeRestController {

	private final RecipeService rService;

	// 레시피 목록
	@GetMapping("/recipe/list")
	// @RequestParam : URL 뒤에 붙는 ?key=value 값을 자바 변수로 받아오는 어노테이션
	// value = "sort" : URL에서 어떤 이름의 파라미터를 찾을지 지정
	// required = false, defaultValue = "latest"
	// 없어도 에러 내지 말고, 없으면 그냥 'latest'라는 값을 넣어놔라
	public ResponseEntity<?> recipe_list(@RequestParam("page") int page,
			@RequestParam(value = "sort", required = false, defaultValue = "latest") String sort) {
		Map<String, Object> resultMap = new HashMap<>(); // 클라이언트 응답용
		Map<String, Object> map = new HashMap<>(); // start, sort 담을 파라미터용 그릇
		int start = (page - 1) * 12;

		// 파라미터용 값 채우기
		map.put("start", start);
		map.put("sort", sort);

		try {
			List<RecipeListVO> list = rService.recipeListData(map);
			int[] pages = rService.pages(page);

			resultMap.put("list", list);
			resultMap.put("pages", pages);

		} catch (Exception ex) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		return ResponseEntity.ok(resultMap);
	}

	// 특정카테고리 조회 + 페이징
	@GetMapping("/recipe/category")
	// recipe_category() => 괄호 안에는 파라미터 선언이 들어가야 하는 자리
	// "" 해서 문자열이 들어가면 안 돼
	// @RequestParam : URL 뒤에 붙는 ?key=value 값을 자바 변수로 받아오는 어노테이션
	// value = "sort" : URL에서 어떤 이름의 파라미터를 찾을지 지정
	// required = false, defaultValue = "latest"
	// 없어도 에러 내지 말고, 없으면 그냥 'latest'라는 값을 넣어놔라
	public ResponseEntity<?> recipe_category(@RequestParam("main_category") String main_category,
			@RequestParam("page") int page,
	        @RequestParam(value = "sort", required = false, defaultValue = "latest") String sort)
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
		   map.put("sort", sort);
		
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
	
	// 좋아요 토글 버튼
	@PostMapping("/recipe/like")
	public ResponseEntity<?> recipe_like(@RequestParam("user_id") int user_id,
	        @RequestParam("recipe_id") int recipe_id)
	{
		// DB 쿼리에 넘길 파라미터를 담는 그릇
	    Map<String, Object> map = new HashMap<>();
	    map.put("user_id", user_id);
	    map.put("recipe_id", recipe_id);

	    // vue한테 돌려줄 응답봉투
	    Map<String, Object> resultMap = new HashMap<>();
	    try {
	        boolean liked = rService.toggleLike(map);
	        resultMap.put("liked", liked); // true=방금 좋아요 눌림, false=방금 좋아요 취소됨
	    } catch (Exception ex) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	    }
	    return ResponseEntity.ok(resultMap);
	}
}