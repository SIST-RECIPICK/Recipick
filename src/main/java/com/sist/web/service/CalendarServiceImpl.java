package com.sist.web.service;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.sist.web.mapper.CalendarMapper;
import com.sist.web.vo.CalendarInfoVO;
import com.sist.web.vo.CalendarItemVO;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class CalendarServiceImpl implements CalendarService{
	private final CalendarMapper cMapper;

	@Override
	public List<CalendarItemVO> selectCalendarItems(int user_id, String year, String month) {
		// TODO Auto-generated method stub
		return cMapper.selectCalendarItems(user_id, year, month);
	}

	@Override
	public void upsertCalendarItem(CalendarItemVO vo) {
		// TODO Auto-generated method stub
		cMapper.upsertCalendarItem(vo);
	}

	@Override
	public CalendarInfoVO selectCalendarInfo(int user_id, String year, String month) {
		// TODO Auto-generated method stub
		// year/month를 정수변환해 달의 일수 계산
		YearMonth ym = YearMonth.of(Integer.parseInt(year), Integer.parseInt(month));
		int daysInMonth = ym.lengthOfMonth();
		
		// info에 칼로리,영양소등 담아둠
		CalendarInfoVO info=cMapper.selectCalendarInfo(user_id, year, month, daysInMonth);
		// top1에 top1 레시피 담아둠
		Map<String, Object> top1 = cMapper.selectTop1Recipe(user_id, year, month);
		if (top1 != null) {
	        info.setTop1_rcp_seq(((Number) top1.get("TOP1_RCP_SEQ")).intValue());
	        info.setTop1_nm((String) top1.get("TOP1_NM"));
	        info.setTop1_count(((Number) top1.get("TOP1_COUNT")).intValue());
	    }
			
		return info;
	}
	

}
