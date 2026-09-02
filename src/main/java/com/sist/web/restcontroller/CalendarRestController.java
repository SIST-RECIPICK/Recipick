package com.sist.web.restcontroller;

import java.util.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sist.web.service.CalendarService;
import com.sist.web.vo.CalendarItemVO;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class CalendarRestController {

	private final CalendarService cService;

	@GetMapping("/calendar/list")
	public ResponseEntity<List<CalendarItemVO>> calendar_list(
			@RequestParam("user_id") int user_id,
			@RequestParam("year") String year,
			@RequestParam("month") String month
	)
	{
		List<CalendarItemVO> list;
		try
		{
			list = cService.selectCalendarItems(user_id, year, month);
		}catch(Exception ex)
		{
			ex.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}		
		return ResponseEntity.ok(list);
	}
	@PostMapping("/calendar/item")
	public ResponseEntity<String> calendar_item_upsert(
			@RequestBody CalendarItemVO vo
	)
	{		
		try
		{
			cService.upsertCalendarItem(vo);
		}catch(Exception ex)
		{
			ex.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}		
		return ResponseEntity.ok("OK");
	}
}