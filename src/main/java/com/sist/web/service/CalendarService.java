package com.sist.web.service;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.sist.web.vo.CalendarInfoVO;
import com.sist.web.vo.CalendarItemVO;

public interface CalendarService {
	public List<CalendarItemVO> selectCalendarItems(
			int user_id,String year,String month);
	public void upsertCalendarItem(CalendarItemVO vo);
	public CalendarInfoVO selectCalendarInfo(int user_id,String year,String month);
}
