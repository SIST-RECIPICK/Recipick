package com.sist.web.restcontroller;

import java.io.Console;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sist.web.security.JwtUser;
import com.sist.web.service.RecipeDetailService;
import com.sist.web.vo.IngredientUnitVO;
import com.sist.web.vo.MyListVO;
import com.sist.web.vo.RecipeLikeVO;
import com.sist.web.vo.RecipeManualVO;
import com.sist.web.vo.RecipeVO;
import com.sist.web.vo.Review_BoardVO;
import com.sist.web.vo.UsersVO;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor

public class RecipeDetailRestController {
	private final RecipeDetailService service;
	
	//레시피 번호를 받아서 상세보기 데이터 전송
	@GetMapping("/recipe/detail")
	public ResponseEntity<Map> recipe_detail(
			@AuthenticationPrincipal JwtUser jwtUser,
			@RequestParam("rcp_seq") int rcp_seq,
			HttpServletResponse response
		) 
	{
	
		//상세보기 입장시 쿠키 저장
		Cookie cookie = new Cookie("recipe_detail_" + rcp_seq, String.valueOf(rcp_seq));
		cookie.setPath("/");
		cookie.setMaxAge(60 * 60 * 24); //1일
		response.addCookie(cookie);
		
		Map map = new HashMap();
		int user_id = 0;
		try {
			if(jwtUser != null)
			{
				user_id = jwtUser.getUserId();
			}
			System.out.println(user_id);
			
			// 내가 등록한 레시피 조회수 증가
			service.recipeHitUp(rcp_seq);
			//좋아요 유무
			int likeExist = service.recipeDetailLikeExist(rcp_seq,user_id);
			
			//북마크 유무
			int markExist = service.recipeDetailBookmarkExist(rcp_seq,user_id);
			
			//작성자,조회수,칼로리,영양정보 등 ...
			RecipeVO recipeData = service.recipeDetailData(rcp_seq);
			
			//레시피 순서
			List<RecipeManualVO> manualList = service.recipeHowList(rcp_seq);
			
			//레시피 재료 리스트
			List<IngredientUnitVO> ingredientUnitList = service.ingredientUnitList(rcp_seq);
			
			map.put("recipeData", recipeData);
			map.put("manualList", manualList);
			map.put("ingredientUnitList", ingredientUnitList);
			map.put("likeExist", likeExist);
			map.put("markExist", markExist);
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		
		return ResponseEntity.ok(map);
	}
	
	@GetMapping("/recipe/cookie")
	public ResponseEntity<Map> recipe_cookie(HttpServletRequest request) 
	{
		
		Cookie[] cookies = request.getCookies();
		
		Map map = new HashMap();
		
		List<RecipeVO> cookieList = new ArrayList<RecipeVO>();
		
		try {
	
			if (cookies != null) {
			    for (Cookie getCookie : cookies) {
			        if (getCookie.getName().startsWith("recipe_detail_")) {
			        	
			        	RecipeVO cookieData = service.recipeDetailData(Integer.parseInt(getCookie.getValue()));
			        	if(cookieData != null)	 
			        		cookieList.add(cookieData);
			        }
			        
			    }
			}
			Collections.reverse(cookieList);

			map.put("cookieList", cookieList);

		} catch (Exception ex) {
			ex.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}

		return ResponseEntity.ok(map);
	}
	
	@GetMapping("/recipe/detail_sub")
	public ResponseEntity<Map> recipe_detail_sub(@RequestParam("rcp_seq") int rcp_seq) 
	{
		Map map = new HashMap();
		
		try {
			
			//연관 리스트 
			List<RecipeVO> relationList = service.relationRecipeList(rcp_seq);
			
			//리뷰 리스트
			List<Review_BoardVO> reviewList = service.recipeReviewList(rcp_seq);
			
			map.put("relationList", relationList);
			map.put("reviewList", reviewList);
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}

		return ResponseEntity.ok(map);
	}
	
	//북마크 좋아요 처리
	@GetMapping("/recipe/interaction")
	public ResponseEntity<?> recipe_interaction(
			@AuthenticationPrincipal JwtUser jwtUser,
			@RequestParam("rcp_seq") int rcp_seq,
			@RequestParam("type") String type
		) 
	{	
		
		int user_id = 0;
		try {
			if(jwtUser != null)
			{
				user_id = jwtUser.getUserId();
			}
			
			if(type.equals("like"))
			{
				int likeExist = service.recipeDetailLikeExist(rcp_seq,user_id);
				
				if(likeExist < 1)
				{
					service.recipeDetailLikeInsert(rcp_seq,user_id);
				}
				else
				{
					service.recipeDetailLikeDelete(rcp_seq,user_id);
				}
			}else 
			{
				int bookmarkExist = service.recipeDetailBookmarkExist(rcp_seq,user_id);
				
				if(bookmarkExist < 1)
				{
					service.recipeDetailBookmarkInsert(rcp_seq, user_id);
				}
				else
				{
					service.recipeDetailBookmarkDelete(rcp_seq, user_id);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		
		return ResponseEntity.ok().build();
	}
	
	
	@GetMapping("/recipe/my-list")
	public ResponseEntity<Map> recipe_my_list(
		@AuthenticationPrincipal JwtUser jwtUser,
		@RequestParam("page") int page,	
		@RequestParam(value = "type",required = false) String type		
	) 
	{	
		if(type == null)
			type ="like";
		
		Map map = new HashMap();
		int user_id = 0;
		try 
		{
			if(jwtUser != null)
			{
				user_id = jwtUser.getUserId();
			}
			if(type.equals("like"))
			{
				List<MyListVO> myLikeList = service.userLikeList(user_id, page);
				int[] pages = service.pages(user_id, page,type);
				map.put("myLikeList", myLikeList);
				map.put("curpage", pages[0]);
				map.put("totalpage", pages[1]);
				map.put("startPage", pages[2]);
				map.put("endPage", pages[3]);
				
			}else
			{
				List<MyListVO> myMarkList = service.userMarkList(user_id, page);
				int[] pages = service.pages(user_id, page,type);
				
				map.put("myMarkList", myMarkList);
				map.put("curpage", pages[0]);
				map.put("totalpage", pages[1]);
				map.put("startPage", pages[2]);
				map.put("endPage", pages[3]);
			}
						
			
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		
		return ResponseEntity.ok(map);
	}
}
