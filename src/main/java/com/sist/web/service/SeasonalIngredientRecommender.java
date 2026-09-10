package com.sist.web.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Service;

import com.sist.web.mapper.AdminMapper;
import com.sist.web.vo.RecIngredientVO;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
public class SeasonalIngredientRecommender {

	private final AdminMapper adminMapper;
	private final ChatClient chatClient;

	public SeasonalIngredientRecommender(ChatClient.Builder builder, AdminMapper adminMapper) {
		this.chatClient = builder.build();
		this.adminMapper = adminMapper;
	}

	// spring ai 호출이 너무 오래걸려서 캐시 기능 추가
	private final Map<String, List<RecIngredientVO>> cache = new ConcurrentHashMap<>();

	public List<RecIngredientVO> recommand(int year, int month) {
		
		String key = year + "-" + month;
		if (cache.containsKey(key)) {
			return cache.get(key);
		}

		List<RecIngredientVO> result = callAi(year, month);
		cache.put(key, result);
		
		return result;
	}

	public List<RecIngredientVO> callAi(int year, int month) {

		List<RecIngredientVO> candidates = adminMapper.findIngNameUsedInRecipes();

		List<String> names = candidates.stream().map(RecIngredientVO::getIngredient_name).toList();

		String prompt = """
				당신은 한식 제철 재료 큐레이터입니다.
				아래 재료 목록 안에서만 %d년 %d월에 가장 제철에 가까운 재료를
				제철 적합도 순으로 최대 10개 골라주세요.

				재료 목록: %s

				규칙:
				- 반드시 목록에 있는 재료명만 그대로 사용
				- [닭고기, 닭고기살] 과 같이 재료명은 다르지만 비슷한 재료를 추천하지 말 것
				- 목록에 없는 재료는 절대 추가하지 말 것
				- 그 달에 가장 대표적인 제철 위주로 골라줄 것
				- 설명 없이 JSON 배열만 출력
				- 형식: ["재료1", "재료2"]
				""".formatted(year, month, String.join(", ", names));

		String json = chatClient.prompt().user(prompt).call().content();
		List<String> aiResult = parseJsonArray(json); // 이름 리스트

		return aiResult.stream().distinct()
				.map(name -> candidates.stream().filter(c -> c.getIngredient_name().equals(name)).findFirst()
						.orElse(null))
				.filter(Objects::nonNull) // AI가 목록 밖 데이터를 주면 filter처리 해버림
				.limit(10).toList();
	}

	private List<String> parseJsonArray(String raw) {
		try {
			String clean = raw.replaceAll("```json", "").replaceAll("```", "").trim();
			ObjectMapper om = new ObjectMapper();
			return om.readValue(clean, new TypeReference<List<String>>() {
			});
		} catch (Exception e) {
			return List.of();
		}
	}

	public String recommandTitle(int month, List<String> ids) {
		String prompt = """
				한식 레시피 큐레이션 제목을 아래 형식에 맞춰 딱 1개만 지어줘.

	            형식: "%d월 제철 [재료]로 차리는 [감성 문구]"
	            재료: %s
	
	            감성 문구 예시 (매번 다르게, 아래를 그대로 쓰지 말고 참고만):
	            - 겨울 밥상
	            - 속 든든한 한 끼
	            - 따뜻한 겨울 식탁
	            - 제철의 깊은 맛
	            - 계절을 담은 밥상
	
	            규칙:
	            - 형식은 지키되 감성 문구는 계절·재료에 어울리게 매번 새롭게
	            - 재료는 쉼표로 구분(예시: 전복, 고등어, 오징어)
	            - [재료] 뒤에는 로, 또는 으로 로 어울리게 작성
	            - 이모지, 특수문자, 과장 표현("최고","궁극") 금지
	            - 전체 30자 내외
	            - 설명 없이 제목 한 줄만 출력 (따옴표 없이)
				""".formatted(month, String.join(", ", ids));
		
		return chatClient.prompt()
		        .user(prompt)
		        .options(ChatOptions.builder()
		                .temperature(0.9))
		        .call()
		        .content()
		        .trim();
	}


}
