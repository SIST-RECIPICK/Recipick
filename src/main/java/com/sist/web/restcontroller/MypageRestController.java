package com.sist.web.restcontroller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sist.web.service.MypageService;
import com.sist.web.vo.UsersVO;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/mypage")
public class MypageRestController {
	private final MypageService mService;

	@GetMapping("/side-profile")
	public ResponseEntity<UsersVO> mypageSideProfile(@RequestParam("userId") int userId) {
		try {
			UsersVO user = mService.mypageSideProfile(userId);
			if (user == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
			}
			return ResponseEntity.ok(user);
		} catch (Exception ex) {
			ex.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GetMapping("/main-count")
	public ResponseEntity<Map<String, Object>> mypageMainCount(@RequestParam("userId") int userId) {
		try {
			Map<String, Object> counts = mService.mypageMainCount(userId);
			return ResponseEntity.ok(counts);
		} catch (Exception ex) {
			ex.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
}
