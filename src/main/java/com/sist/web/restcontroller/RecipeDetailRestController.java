package com.sist.web.restcontroller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sist.web.service.RecipeDetailService;
import com.sist.web.vo.RecipeVO;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@CrossOrigin(value = "*")
public class RecipeDetailRestController {
	private final RecipeDetailService service;
	
	@GetMapping("/recipe/detail")
	public ResponseEntity<Map> recipe_detail(@RequestParam("rcp_seq") int rcp_seq)
	{
		Map map = new HashMap();
		
		try 
		{
			RecipeVO vo = service.recipeDetailData(rcp_seq);
			
			map.put("recipeData", vo);
			
		}catch(Exception ex)
		{
			ex.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		
		return ResponseEntity.ok(map);
	}
}
