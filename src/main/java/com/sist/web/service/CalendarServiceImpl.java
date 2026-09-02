package com.sist.web.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sist.web.mapper.CalendarMapper;
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
	

}
