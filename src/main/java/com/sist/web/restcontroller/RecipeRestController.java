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

}
