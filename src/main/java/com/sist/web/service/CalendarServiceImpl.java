package com.sist.web.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sist.web.mapper.CalendarMapper;
import com.sist.web.vo.CalendarItemVO;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class CalendarServiceImpl implements CalendarService{
	private final CalendarMapper mapper;

	@Override
	public List<CalendarItemVO> selectCalendarItems(int user_id, String year, String month) {
		// TODO Auto-generated method stub
		return mapper.selectCalendarItems(user_id, year, month);
	}
	

}
