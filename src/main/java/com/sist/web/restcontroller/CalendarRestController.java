package com.sist.web.restcontroller;

import java.util.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sist.web.security.JwtUser;
import com.sist.web.service.CalendarService;
import com.sist.web.vo.CalendarInfoVO;
import com.sist.web.vo.CalendarItemVO;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class CalendarRestController {

	private final CalendarService cService;

	@GetMapping("/calendar/list")
	public ResponseEntity<List<CalendarItemVO>> calendar_list(
			@AuthenticationPrincipal JwtUser jwtUser,
			@RequestParam("year") String year,
			@RequestParam("month") String month
	)
	{
		List<CalendarItemVO> list;
		try
		{
			list = cService.selectCalendarItems(jwtUser.getUserId(), year, month);
		}catch(Exception ex)
		{
			ex.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}		
		return ResponseEntity.ok(list);
	}
	@PostMapping("/calendar/item")
	public ResponseEntity<String> calendar_item_upsert(
			@AuthenticationPrincipal JwtUser jwtUser,
			@RequestBody CalendarItemVO vo
	)
	{		
		if (jwtUser == null) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	    }

	    vo.setUser_id(jwtUser.getUserId());
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
	@GetMapping("/calendar/info")
	public ResponseEntity<CalendarInfoVO> calendar_info(
			@AuthenticationPrincipal JwtUser jwtUser,
			@RequestParam("year") String year,
			@RequestParam("month") String month
			)
	{	
		CalendarInfoVO info;
		try
		{
			info = cService.selectCalendarInfo(jwtUser.getUserId(), year, month);
		}catch(Exception ex)
		{
			ex.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		return ResponseEntity.ok(info);
	}
	@DeleteMapping("/calendar/item")
	public ResponseEntity<Integer> calendar_item_delete(
			@AuthenticationPrincipal JwtUser jwtUser,
			@RequestParam("meal_date") String meal_date,
			@RequestParam("meal_type") String meal_type
			)
	{
		int result;
		try
		{
			result = cService.deleteCalendarItem(jwtUser.getUserId(), meal_date, meal_type);
		}catch(Exception ex)
		{
			ex.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		return ResponseEntity.ok(result);
	}
}