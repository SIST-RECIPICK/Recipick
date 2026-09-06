package com.sist.web.restcontroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sist.web.service.RecipeDetailService;
import com.sist.web.vo.IngredientUnitVO;
import com.sist.web.vo.RecipeManualVO;
import com.sist.web.vo.RecipeVO;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@CrossOrigin(
originPatterns = "*",
allowCredentials = "true"
)
public class RecipeDetailRestController {
	private final RecipeDetailService service;

	//레시피 번호를 받아서 상세보기 데이처 전송
	@GetMapping("/recipe/detail")
	public ResponseEntity<Map> recipe_detail(
			@RequestParam("rcp_seq") int rcp_seq,
			HttpServletResponse response,
			HttpServletRequest request
		) 
	{
		//상세보기 입장시 쿠키 저장
		Cookie cookie = new Cookie("recipe_detail_" + rcp_seq, String.valueOf(rcp_seq));
		cookie.setPath("/");
		cookie.setMaxAge(60 * 60 * 24); //1일
		response.addCookie(cookie);
		
		Cookie[] cookies = request.getCookies();
		int cookieNo = 1;
		Map map = new HashMap();
		
		List<RecipeVO> cookieList = new ArrayList<RecipeVO>();
		
		try {
	
			if (cookies != null) {
			    for (Cookie getCookie : cookies) {
			    	System.out.println(getCookie);
			        if (getCookie.getName().startsWith("recipe_detail_")) {
			        	
			        	RecipeVO cookieData = service.recipeDetailData(Integer.parseInt(getCookie.getValue()));
			        	System.out.println(getCookie.getValue());
			        	cookieList.add(cookieData);
			        	cookieNo++; //방문 기록은 6개까지만 보여준다
			        }
			        if(cookieNo > 6)
		        	{
		        		break;
		        	}
			    }
			}
			//작성자,조회수,칼로리,영양정보 등 ...
			RecipeVO recipeData = service.recipeDetailData(rcp_seq);
			
			//레시피 순서
			List<RecipeManualVO> manualList = service.recipeHowList(rcp_seq);
			
			//레시피 재료 리스트
			List<IngredientUnitVO> ingredientUnitList = service.ingredientUnitList(rcp_seq);

			map.put("recipeData", recipeData);
			map.put("manualList", manualList);
			map.put("ingredientUnitList", ingredientUnitList);
			map.put("cookieList", cookieList);
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}

		return ResponseEntity.ok(map);
	}
}
