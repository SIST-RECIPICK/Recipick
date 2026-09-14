package com.sist.web.scheduler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sist.web.mapper.AuthMapper;
import com.sist.web.service.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// [하드탈퇴 배치] - 소프트탈퇴 후 30일 경과한 계정을 매일 자정 익명화 처리
@Slf4j
@Component
@RequiredArgsConstructor
public class HardWithdrawScheduler {

	private static final int HARD_DELETE_GRACE_PERIOD_DAYS = 30;

	private final AuthMapper authMapper;
	private final AuthService authService;

	@Scheduled(cron = "0 0 0 * * *")
	public void run() {
		LocalDateTime cutoffDate = LocalDateTime.now().minusDays(HARD_DELETE_GRACE_PERIOD_DAYS);
		List<Integer> candidates = authMapper.findHardDeleteCandidates(cutoffDate);

		int processedCount = 0;
		int skippedCount = 0;
		List<Integer> failedUserIds = new ArrayList<>();

		for (Integer userId : candidates) {
			try {
				// AuthService(별도 빈)를 통해 호출해야 @Transactional 프록시가 적용되어 건당 트랜잭션이 분리됨
				if (authService.anonymizeUser(userId)) {
					processedCount++;
				} else {
					skippedCount++;
				}
			} catch (Exception e) {
				// 한 명 실패가 나머지 처리를 막지 않도록 격리
				failedUserIds.add(userId);
				log.error("하드탈퇴 처리 실패 - userId: {}", userId, e);
			}
		}

		log.info("하드탈퇴 배치 완료 - processedCount: {}, skippedCount: {}, failedUserIds: {}",
				processedCount, skippedCount, failedUserIds);
	}
}
