package com.sist.web.restcontroller;

import java.net.ResponseCache;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.sist.web.security.JwtUser;
import com.sist.web.service.CalendarAiService;
import com.sist.web.vo.FillResultVO;
import com.sist.web.vo.RollbackResultVO;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CalendarAiRestController {
	private final CalendarAiService aiService;
	
	@PostMapping("/calendar/ai-fill")
	public ResponseEntity<FillResultVO> ai_fill(
			@AuthenticationPrincipal JwtUser jwtUser,
			@RequestBody Map<String,String> body
	){
		if(jwtUser == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		
		FillResultVO result;
        try {
            String year = body.get("year");
            String month = body.get("month");
            String command = body.get("command");
            result = aiService.fillEmptySlots(jwtUser.getUserId(), year, month, command);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok(result);
    }
	
	@PostMapping("/calendar/ai-fill/rollback")
    public ResponseEntity<RollbackResultVO> ai_rollback(
            @AuthenticationPrincipal JwtUser jwtUser
    ) {
        if (jwtUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        RollbackResultVO result;
        try {
            result = aiService.rollback(jwtUser.getUserId());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok(result);
    }
	@PostMapping("/calendar/ai-fill/confirm")
	public ResponseEntity<String> ai_confirm(
			@AuthenticationPrincipal JwtUser jwtUser
	){
		if (jwtUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
		try {
			aiService.confirm(jwtUser.getUserId());
		}catch(Exception ex) {
			ex.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		return ResponseEntity.ok("OK");
	}
}
