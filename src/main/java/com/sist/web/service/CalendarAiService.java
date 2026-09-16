package com.sist.web.service;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import com.sist.web.vo.FillResultVO;
import com.sist.web.vo.RollbackResultVO;

import lombok.RequiredArgsConstructor;

public interface CalendarAiService {
	public FillResultVO fillEmptySlots(int user_id, String year, String month, String command);
	public RollbackResultVO rollback(int user_id);
}
