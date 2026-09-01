package com.sist.web.restcontroller;

import java.util.List;

import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sist.web.service.RecipeSearchService;
import com.sist.web.vo.RecipeSearchVO;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class RecipeSearchController {
	private final RecipeSearchService rsService;
	
	@GetMapping("recipe/search")
	public ResponseEntity<List<RecipeSearchVO>> recipe_search(
			@RequestParam("keyword") String keyword,
			@RequestParam("user_id") int user_id
	)
	{
		List<RecipeSearchVO> list;
		try
		{
			list=rsService.selectRecipeSearch(keyword, user_id);
		}catch(Exception ex)
		{
			ex.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		return ResponseEntity.ok(list);
	}
}
