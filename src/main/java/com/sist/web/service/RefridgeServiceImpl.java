package com.sist.web.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import com.sist.web.mapper.RecipeLikeMapper;
import com.sist.web.mapper.RefridgeMapper;
import com.sist.web.postgres.PostgresRecipeMapper;
import com.sist.web.vo.IngredientVO;
import com.sist.web.vo.RefridgeVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefridgeServiceImpl implements RefridgeService {
	private final RefridgeMapper rMapper;
	private final PostgresRecipeMapper prMapper;
	private final EmbeddingModel eModel;
	private final IngredientService iService;
	private final RecipeLikeMapper likeMapper;

	@Override
	public void registerData(List<RefridgeVO> volist) {
		for (RefridgeVO vo : volist) {
			System.out.println("vo = " + vo);
			rMapper.registerData(vo);
		}
	}

	@Override
	public List<RefridgeVO> fridgeData(int users_id) {
		return rMapper.fridgeData(users_id);
	}

	@Override
	public List<Map<String, Object>> recommandRecipe(List<String> ingredients, String sort) {
		// 전체 재료 목록을 한 번만 조회해서 재사용 (레시피마다 반복 조회하지 않도록)
		List<IngredientVO> allIngredients = iService.findAll();

		if (ingredients == null || ingredients.isEmpty()) {
			return Collections.emptyList();
		}

		// 사용자가 선택한 재료로 검색용 문장 생성
		String queryText = createQueryText(ingredients);

		// 문장을 임베딩(벡터)으로 변환
		float[] vector = eModel.embed(queryText);
		String vectorString = convertVectorToString(vector);

		// 벡터 유사도 검색 - 필터링으로 일부가 제외될 것을 감안해 여유분을 넉넉히 가져옴
		List<Map<String, Object>> recipes = prMapper.findSimilarRecipes(vectorString,5);

		// 레시피별로 재료 충족 정보 및 부가 정보 채우기
		for (Map<String, Object> recipe : recipes) {
			Object contentObject = recipe.get("content");
			String content = "";
			if (contentObject != null) {
				content = contentObject.toString();
			}

			// 레시피 content에서 실제로 어떤 재료가 들어있는지 추출
			List<String> recipeIngredients = extractIngredients(content, allIngredients);

			// 사용자가 가진 재료와 레시피 재료를 비교해 충족률 계산
			Map<String, Object> ingredientStatus = calculateIngredientStatus(ingredients, recipeIngredients);

			recipe.put("haveIngredients", ingredientStatus.get("haveIngredients"));
			recipe.put("missingIngredients", ingredientStatus.get("missingIngredients"));
			recipe.put("ingredientRate", ingredientStatus.get("ingredientRate"));

			// 레시피 이름 추출
			recipe.put("recipeName", extractRecipeName(content));

			// 조리방법 추출
			recipe.put("cookingMethod", extractValue(content, "조리방법"));

			// 요리종류 추출
			recipe.put("foodType", extractValue(content, "요리종류"));

			// 요리팁 추출
			recipe.put("tip", extractValue(content, "요리팁"));

			// 조리정보 (이미지 URL)
			recipe.put("recipeImage", extractValue(content, "조리정보"));

			// 조리 과정 추출
			recipe.put("steps", extractCookingSteps(content));
		}

		// 매칭된 재료가 하나도 없는 레시피는 제외 (벡터 검색만으론 재료 무관 레시피가 섞여 나올 수 있음)
		recipes = recipes.stream()
			.filter(recipe -> {
				List<?> have = (List<?>) recipe.get("haveIngredients");
				return have != null && !have.isEmpty();
			})
			.collect(Collectors.toList());
		
		if ("popular".equals(sort)) {
	        List<Map<String, Object>> likeCounts = likeMapper.countLikesByRecipe();
	        Map<Long, Long> likeMap = new HashMap<>();
	        for (Map<String, Object> row : likeCounts) {
	            likeMap.put(
	                ((Number) row.get("recipe_id")).longValue(),
	                ((Number) row.get("like_count")).longValue()
	            );
	        }
	        for (Map<String, Object> recipe : recipes) {
	            Long recipeId = ((Number) recipe.get("recipe_id")).longValue();
	            recipe.put("likeCount", likeMap.getOrDefault(recipeId, 0L));
	        }
	        recipes.sort((a, b) -> {
	            Long likeA = (Long) a.get("likeCount");
	            Long likeB = (Long) b.get("likeCount");
	            return likeB.compareTo(likeA);
	        });
	    } else {
	        recipes.sort((a, b) -> {
	            List<?> haveA = (List<?>) a.get("haveIngredients");
	            List<?> haveB = (List<?>) b.get("haveIngredients");
	            return haveB.size() - haveA.size();
	        });
	    }

		// 매칭된 재료 개수가 많은 순으로 재정렬 (사용자가 가진 재료를 더 많이 활용하는 레시피 우선)
		recipes.sort((a, b) -> {
			List<?> haveA = (List<?>) a.get("haveIngredients");
			List<?> haveB = (List<?>) b.get("haveIngredients");
			return haveB.size() - haveA.size();
		});

		// 최종적으로 상위 7개만 반환
		//recipes = recipes.stream().limit(7).collect(Collectors.toList());

		return recipes;
	}

	private String createQueryText(List<String> ingredients) {
		StringBuilder sb = new StringBuilder();

		sb.append("냉장고에 있는 재료를 이용할 수 있는 레시피");
		sb.append("사용 가능한 재료: ");

		for (String ingredient : ingredients) {
			if (ingredient == null) {
				continue;
			}

			String value = ingredient.trim();

			if (!value.isEmpty()) {
				sb.append(value).append(" ");
			}
		}

		return sb.toString().trim();
	}

	/**
	 * ========================================================
	 * float[] → PostgreSQL vector 문자열
	 * ========================================================
	 *
	 * 예:
	 *
	 * [0.123,-0.234,0.345]
	 */
	private String convertVectorToString(float[] vector) {
		if (vector == null || vector.length == 0) {
			throw new IllegalArgumentException("Embedding vector가 비어 있습니다.");
		}

		StringBuilder sb = new StringBuilder();
		sb.append("[");

		for (int i = 0; i < vector.length; i++) {
			if (i > 0) {
				sb.append(",");
			}
			sb.append(vector[i]);
		}

		sb.append("]");

		return sb.toString();
	}

	/**
	 * ========================================================
	 * 레시피 이름 추출
	 * ========================================================
	 *
	 * content 예:
	 *
	 * 레시피명: 버섯 두유 소스 볶음
	 *
	 * → 버섯 두유 소스 볶음
	 */
	private String extractRecipeName(String content) {
		String value = extractValue(content, "레시피명");

		if (value.isEmpty()) {
			return "추천 레시피";
		}

		return value;
	}

	/**
	 * ========================================================
	 * content에서 특정 항목 추출
	 * ========================================================
	 *
	 * 예:
	 *
	 * 조리방법: 볶기
	 *
	 * → 볶기
	 */
	private String extractValue(String content, String key) {
		if (content == null || content.isEmpty()) {
			return "";
		}

		String[] lines = content.split("\\r?\\n");

		for (String line : lines) {
			String trim = line.trim();

			if (trim.startsWith(key + ":")) {
				return trim.substring((key + ":").length()).trim();
			}
		}

		return "";
	}

	/**
	 * ========================================================
	 * 레시피 재료 추출
	 * ========================================================
	 *
	 * 전체 재료 목록(DB)에서 이름이 content에 포함되는지 검사한다.
	 * allIngredients는 recommandRecipe에서 한 번만 조회해 반복 호출 시 재사용한다.
	 */
	private List<String> extractIngredients(String content, List<IngredientVO> allIngredients) {
		if (content == null || content.isEmpty()) {
			return Collections.emptyList();
		}

		Set<String> ingredients = new HashSet<>();

		for (IngredientVO vo : allIngredients) {
			String name = vo.getIngredient_name();
			if (name != null && content.contains(name)) {
				ingredients.add(name);
			}
		}

		return new ArrayList<>(ingredients);
	}

	/**
	 * ========================================================
	 * 재료 충족률 계산
	 * ========================================================
	 *
	 * 예:
	 *
	 * 필요한 재료 김치 돼지고기 두부 대파 양파 고춧가루
	 *
	 * 가지고 있는 재료 김치 돼지고기 두부 대파 양파
	 *
	 * 충족률
	 *
	 * 5 / 6 × 100 = 83.3%
	 */
	private Map<String, Object> calculateIngredientStatus(List<String> userIngredients,
			List<String> recipeIngredients) {

		Map<String, Object> result = new HashMap<>();

		List<String> have = new ArrayList<>();
		List<String> missing = new ArrayList<>();

		// 레시피 재료가 없으면 계산할 수 없으므로 0%
		if (recipeIngredients == null || recipeIngredients.isEmpty()) {
			result.put("haveIngredients", have);
			result.put("missingIngredients", missing);
			result.put("ingredientRate", 0.0);
			return result;
		}

		// 사용자 재료를 정규화한 Set으로 만든다.
		Set<String> userSet = new HashSet<>();

		for (String ingredient : userIngredients) {
			if (ingredient != null) {
				userSet.add(normalizeIngredient(ingredient));
			}
		}

		// 레시피 재료 비교
		for (String recipeIngredient : recipeIngredients) {
			String normalized = normalizeIngredient(recipeIngredient);
			boolean exists = false;

			for (String userIngredient : userSet) {
				// 예: 새송이버섯 / 버섯 - 일부 포함 관계도 인정
				if (normalized.contains(userIngredient) || userIngredient.contains(normalized)) {
					exists = true;
					break;
				}
			}

			if (exists) {
				have.add(recipeIngredient);
			} else {
				missing.add(recipeIngredient);
			}
		}

		// 충족률 (소수점 1자리)
		double rate = ((double) have.size() / recipeIngredients.size()) * 100.0;
		rate = Math.round(rate * 10.0) / 10.0;

		result.put("haveIngredients", have);
		result.put("missingIngredients", missing);
		result.put("ingredientRate", rate);

		return result;
	}

	/**
	 * ========================================================
	 * 재료 문자열 정규화
	 * ========================================================
	 */
	private String normalizeIngredient(String ingredient) {
		if (ingredient == null) {
			return "";
		}

		return ingredient
			.replaceAll("[0-9]+(\\.\\d+)?", "")
			.replaceAll("(g|kg|ml|L|개|모|대|큰술|작은술|컵|쪽|약간)", "")
			.replaceAll("\\([^)]*\\)", "")
			.trim()
			.toLowerCase();
	}

	/**
	 * ========================================================
	 * 조리 과정 추출
	 * ========================================================
	 *
	 * DB content의 문장 중 실제 조리 설명을 화면에 출력하기 위한 메소드
	 *
	 * 현재 데이터가 구조화되어 있지 않은 경우 content의 문장을 분리한다.
	 */
	private List<String> extractCookingSteps(String content) {
		List<String> steps = new ArrayList<>();

		if (content == null || content.isEmpty()) {
			return steps;
		}

		// "조리정보" 이후는 이미지 URL이므로 제외한다.
		String cookingPart = content;
		int imageIndex = cookingPart.indexOf("조리정보:");

		if (imageIndex >= 0) {
			cookingPart = cookingPart.substring(0, imageIndex);
		}

		// 문장을 분리한다.
		String[] sentences = cookingPart.split("(?<=[.!?])\\s+");

		for (String sentence : sentences) {
			String value = sentence.trim();

			// 너무 짧은 문장 제외
			if (value.length() >= 5
					&& !value.startsWith("레시피명:")
					&& !value.startsWith("영양정보:")
					&& !value.startsWith("탄수화물:")
					&& !value.startsWith("단백질:")
					&& !value.startsWith("지방:")
					&& !value.startsWith("나트륨:")
					&& !value.startsWith("해시태그:")
					&& !value.startsWith("주재료:")) {
				steps.add(value);
			}

			// 화면이 너무 길어지는 것을 방지
			if (steps.size() >= 6) {
				break;
			}
		}

		return steps;
	}
}