package com.sist.web.restcontroller;

import java.util.List;

import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sist.web.security.JwtUser;
import com.sist.web.service.RecipeSearchService;
import com.sist.web.vo.RecipePreviewVO;
import com.sist.web.vo.RecipeSearchVO;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class RecipeSearchController {
	private final RecipeSearchService rsService;
	
	@GetMapping("recipe/search")
	public ResponseEntity<List<RecipeSearchVO>> recipe_search(
			@RequestParam("keyword") String keyword,  // 검색 값과 유저 아이디 받아옴
			@AuthenticationPrincipal JwtUser jwtUser
	)
	{
		List<RecipeSearchVO> list; // 리스트 생성
		try
		{
			list=rsService.selectRecipeSearch(keyword, jwtUser.getUserId());
		}catch(Exception ex)
		{
			ex.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		return ResponseEntity.ok(list);
	}
	@GetMapping("recipe/preview")
	public ResponseEntity<RecipePreviewVO> recipe_preview(
			@RequestParam("rcp_seq") int rcp_seq
	)
	{
		RecipePreviewVO preview;
		try
		{
			preview = rsService.selectRecipePreview(rcp_seq);
		}catch(Exception ex)
		{
			ex.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		return ResponseEntity.ok(preview);
	}
}
