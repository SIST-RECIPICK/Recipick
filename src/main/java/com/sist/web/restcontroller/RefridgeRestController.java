package com.sist.web.restcontroller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.sist.web.service.RefridgeService;
import com.sist.web.vo.RecipeVO;
import com.sist.web.vo.RefridgeVO;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/refrige")
public class RefridgeRestController {
	private final RefridgeService rfService;

	@PostMapping("/register")
	public void register(@RequestBody List<RefridgeVO> volist) {
		rfService.registerData(volist);
	}

	@GetMapping("/fridgedata")
	public List<RefridgeVO> fridgedata(@RequestParam("user_id") int user_id) {
		return rfService.fridgeData(user_id);
	}

	/**
	 * ======================================================== 레시피 Vector 검색
	 * ========================================================
	 *
	 * POST
	 *
	 * /recipe/recommand
	 *
	 * JSON
	 *
	 * { "ingredients": [ "김치", "돼지고기", "두부" ] }
	 */
	@PostMapping("/recommand")
	@ResponseBody
	public Map<String, Object> recommand(@RequestBody Map<String, Object> request) {

	    Map<String, Object> response = new HashMap<>();

	    try {
	        Object ingredientObject = request.get("ingredients");

	        if (ingredientObject == null) {
	            response.put("success", false);
	            response.put("message", "재료를 선택해주세요.");
	            response.put("recipes", Collections.emptyList());
	            return response;
	        }

	        List<String> ingredients = new ArrayList<>();
	        if (ingredientObject instanceof List<?>) {
	            List<?> list = (List<?>) ingredientObject;
	            for (Object value : list) {
	                if (value != null) {
	                    String ingredient = value.toString().trim();
	                    if (!ingredient.isEmpty()) {
	                        ingredients.add(ingredient);
	                    }
	                }
	            }
	        }

	        System.out.println("검색 요청 재료: " + ingredients);   // 위치도 여기로 이동 (실제 채운 뒤에 로그 찍기)

	        if (ingredients.isEmpty()) {
	            response.put("success", false);
	            response.put("message", "재료를 한 개 이상 선택해주세요.");
	            response.put("recipes", Collections.emptyList());
	            return response;
	        }

	        // sort 값을 request(body)에서 꺼냄
	        Object sortObject = request.get("sort");
	        String sort = (sortObject != null) ? sortObject.toString() : "match";

	        List<Map<String, Object>> recipes = rfService.recommandRecipe(ingredients, sort);

	        for (Map a : recipes) {
	            System.out.println("==================>" + a.toString());
	        }

	        response.put("success", true);
	        response.put("message", recipes.isEmpty() ? "추천 레시피가 없습니다." : "레시피 추천이 완료되었습니다.");
	        response.put("recipes", recipes);
	        response.put("selectedIngredients", ingredients);

	        return response;

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.put("success", false);
	        response.put("message", "레시피 검색 중 오류가 발생했습니다.");
	        response.put("recipes", Collections.emptyList());
	        return response;
	    }
	    
	      
	}
	
//	@GetMapping("/recipe/{rcp_seq}")
//	public RecipeVO getRecipeDetail(@PathVariable int rcp_seq) {
//		
//	    return rfService.oracleRecipeAllData(rcp_seq);
//	}
}