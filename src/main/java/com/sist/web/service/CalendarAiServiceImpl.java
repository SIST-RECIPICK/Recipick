package com.sist.web.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sist.web.mapper.CalendarMapper;
import com.sist.web.postgres.AiFillHistoryMapper;
import com.sist.web.vo.CalendarItemVO;
import com.sist.web.vo.FillResultVO;
import com.sist.web.vo.FilledSlotVO;
import com.sist.web.vo.RollbackResultVO;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.Date;
import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class CalendarAiServiceImpl implements CalendarAiService{
	private final CalendarMapper cMapper;
	private final AiFillHistoryMapper aMapper; 
	private final ChatClient.Builder chatClientBuilder;
	
	@Override
	public FillResultVO fillEmptySlots(int user_id, String year, String month, String command) {
		// TODO Auto-generated method stub
		List<Map<String, Object>> emptySlots = cMapper.selectEmptySlots(user_id, year, month);
		if (emptySlots.isEmpty()) {
	        // 빈 슬롯이 아예 없으면, 더 진행할 필요 없이 빈 결과 리턴
	        FillResultVO result = new FillResultVO();
	        result.setFilledCount(0);
	        result.setFilledSlots(new ArrayList<>());
	        return result;
	    }
		List<Map<String, Object>> candidates = cMapper.selectRecipeCandidates();
		System.out.println("candidates 개수: " + candidates.size());
		//후보가 너무 많을시 추리기
		if (candidates.size() > 50) {
		    Collections.shuffle(candidates);
		    candidates = candidates.subList(0, 50);
		}
		// 프롬프트 구성
		String prompt = buildPrompt(emptySlots, candidates, command);
		System.out.println("=== 전송한 프롬프트 (앞부분만) ===");          
	    System.out.println(prompt.substring(0, Math.min(500, prompt.length())));
	    System.out.println("=== 프롬프트 총 길이: " + prompt.length() + " ===");
	    ChatClient chatClient = chatClientBuilder.build();
	    String aiResponse = chatClient.prompt()
	            .system("당신은 식단 추천 도우미입니다. 반드시 JSON 배열 형식으로만 응답하세요. 다른 설명은 절대 포함하지 마세요.")
	            .user(prompt)
	            .call()
	            .content();
	    
	    System.out.println("=== Gemini 원본 응답 ===");
	    System.out.println(aiResponse);
	    System.out.println("========================");
	    System.out.println("=== 전체 프롬프트 ===");
	    System.out.println(prompt);
	    System.out.println("=== 프롬프트 끝 ===");
	    String cleanJson = aiResponse.replaceAll("```json", "").replaceAll("```", "").trim();

	    List<Map<String, Object>> plans;
	    try {
	        ObjectMapper mapper = new ObjectMapper();
	        plans = mapper.readValue(cleanJson, new TypeReference<List<Map<String, Object>>>() {});
	    } catch (Exception e) {
	        e.printStackTrace();
	        FillResultVO result = new FillResultVO();
	        result.setFilledCount(0);
	        result.setFilledSlots(new ArrayList<>());
	        return result;
	    }
	 // 레시피 이름 조회용 맵 만들기 (rcp_seq -> rcp_nm)
	    Map<Integer, String> recipeNameMap = new HashMap<>();
	    for (Map<String, Object> r : candidates) {
	    	recipeNameMap.put(((Number) r.get("RCP_SEQ")).intValue(), (String) r.get("RCP_NM"));
	    }

	    List<FilledSlotVO> filledSlots = new ArrayList<>();

	    for (Map<String, Object> plan : plans) {
	        String meal_date = plan.get("meal_date").toString();
	        System.out.println("Gemini가 준 meal_date 원본: " + meal_date);
	        
	        String meal_type = plan.get("meal_type").toString();
	        int rcp_seq = ((Number) plan.get("rcp_seq")).intValue();
	        
	        Date parsedDate;
	        try {
	            parsedDate = Date.valueOf(meal_date);
	            System.out.println("Date.valueOf 결과: " + parsedDate);
	        } catch (Exception e) {
	            e.printStackTrace();
	            continue; // 날짜 파싱 실패하면 이 슬롯은 건너뜀
	        }
	        
	        // 1. 실제 캘린더에 배치 (Oracle)
	        CalendarItemVO vo = new CalendarItemVO();
	        vo.setUser_id(user_id);
	        vo.setMeal_date(parsedDate); // Date 객체로 넣음
	        vo.setMeal_type(meal_type);
	        vo.setRcp_seq(rcp_seq);
	        
	        cMapper.upsertCalendarItem(vo);

	        // 2. 이력 기록 (Postgres)
	        aMapper.insertFillHistory(user_id, meal_date, meal_type, rcp_seq);
	        
	        // 3. 결과 목록에 추가
	        FilledSlotVO slot = new FilledSlotVO();
	        slot.setMeal_date(meal_date);
	        slot.setMeal_type(meal_type);
	        slot.setRcp_seq(rcp_seq);
	        slot.setRcp_nm(recipeNameMap.getOrDefault(rcp_seq, "알 수 없음"));
	        filledSlots.add(slot);
	    }

	    FillResultVO result = new FillResultVO();
	    result.setFilledCount(filledSlots.size());
	    result.setFilledSlots(filledSlots);
	    return result;
	}
	
	@Override
	public RollbackResultVO rollback(int user_id) {
		// TODO Auto-generated method stub
		// 목록 가져오기

		List<Map<String, Object>> history = aMapper.selectRecentFillHistory(user_id);

		// 목록 하나씩 돌면서 기존 삭제 메서드로 지우기
		int count = 0;
		for(Map<String,Object> item : history) {
			String meal_date = item.get("meal_date").toString();
			String meal_type = item.get("meal_type").toString();
			
			cMapper.deleteCalendarItem(user_id, meal_date, meal_type);
			count++;
		}

		// 롤백 처리 표시
		aMapper.markAsRolledBack(user_id);
		
		// 결과 만들어서 리턴
		RollbackResultVO result = new RollbackResultVO();
		result.setRolledBackCount(count);
		result.setSuccess(true);
		return result;
	}
	
	private String buildPrompt(List<Map<String, Object>> emptySlots, List<Map<String, Object>> candidates, String command) {
	    StringBuilder sb = new StringBuilder();
	    sb.append("사용자 요청: ").append(command).append("\n\n");
	    
	    sb.append("채워야 할 빈 슬롯 목록:\n");
	    for (Map<String, Object> slot : emptySlots) {
	        sb.append("- ").append(slot.get("MEAL_DATE")).append(" ").append(slot.get("MEAL_TYPE")).append("\n");
	    }
	    
	    sb.append("\n선택 가능한 레시피 목록 (rcp_seq, 이름, 칼로리, 단백질):\n");
	    for (Map<String, Object> r : candidates) {
	        sb.append("- ").append(r.get("RCP_SEQ")).append(", ")
	          .append(r.get("RCP_NM")).append(", ")
	          .append(r.get("INFO_ENG")).append("kcal, ")
	          .append(r.get("INFO_PRO")).append("g단백질\n");
	    }
	    
	    sb.append("\n위 조건에 맞게, 빈 슬롯 중 일부(또는 전부)를 레시피로 채워주세요.");
	    sb.append("\n반드시 아래 JSON 배열 형식으로만 응답하세요:");
	    sb.append("\n[{\"meal_date\":\"2026-09-05\",\"meal_type\":\"아침\",\"rcp_seq\":304}, ...]");
	    
	    return sb.toString();
	}

	@Override
	public void confirm(int user_id) {
		// TODO Auto-generated method stub
		aMapper.confirmFill(user_id);
	}

}
