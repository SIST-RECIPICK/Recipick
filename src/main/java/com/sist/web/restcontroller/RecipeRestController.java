package com.sist.web.restcontroller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sist.web.service.RecipeService;
import com.sist.web.vo.RecipeVO;

import lombok.RequiredArgsConstructor;
@RestController
@RequiredArgsConstructor
public class RecipeRestController {

	private final RecipeService rService;
	
	 @GetMapping("/recipe/list")
	 public ResponseEntity<List<RecipeVO>> recipe_list(@RequestParam("start") int start)
	 {
		 List<RecipeVO> list = null;
		 
		 try 
		 {
		   list = rService.recipeListData(start);
			
		
		 } 
		 catch (Exception ex) 
		 {
			ex.printStackTrace();
		}
		 return ResponseEntity.ok(list);
	 }
	
}
