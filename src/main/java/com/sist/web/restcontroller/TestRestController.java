package com.sist.web.restcontroller;

import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sist.web.scheduler.HardWithdrawScheduler;

import lombok.RequiredArgsConstructor;

// TEST ONLY - 커밋 전 제거. 하드탈퇴 배치를 자정까지 기다리지 않고 수동으로 트리거하기 위한 임시 엔드포인트.
@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class TestRestController {

	private final HardWithdrawScheduler hardWithdrawScheduler;

	// TEST ONLY - 커밋 전 제거
	@GetMapping("/trigger-hard-withdraw")
	public Map<String, String> triggerHardWithdraw() {
		hardWithdrawScheduler.run();
		return Map.of("message", "하드탈퇴 배치를 실행했습니다. 결과는 애플리케이션 로그를 확인하세요.");
	}
}
