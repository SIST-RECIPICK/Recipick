package com.sist.web.restcontroller;

import java.util.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sist.web.service.MypageService;
import com.sist.web.vo.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/mypage")
public class MypageRestController {
	private final MypageService mService;

	@GetMapping("/profile")
	public ResponseEntity<UsersVO> mypageProfile(@RequestParam("id") int id) {
		try {
			UsersVO user = mService.mypageProfile(id);
			if (user == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
			}
			return ResponseEntity.ok(user);
		} catch (Exception ex) {
			ex.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GetMapping("/main_count")
	public ResponseEntity<Map<String, Object>> mypageMainCount(@RequestParam("id") int id) {
		try {
			Map<String, Object> counts = mService.mypageMainCount(id);
			return ResponseEntity.ok(counts);
		} catch (Exception ex) {
			ex.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	@GetMapping("/reviews")
	public ResponseEntity<List<Review_BoardVO>> myReviewList(
			@RequestParam("id") int id,
			@RequestParam(value = "page", defaultValue = "1") int page) {
		try {
			List<Review_BoardVO> list = mService.myReviewList(id, page);
			return ResponseEntity.ok(list);
		} catch (Exception ex) {
			ex.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GetMapping("/replies")
	public ResponseEntity<List<Review_Board_ReplyVO>> myReviewReplyList(
			@RequestParam("id") int id,
			@RequestParam(value = "page", defaultValue = "1") int page) {
		try {
			List<Review_Board_ReplyVO> list = mService.myReviewReplyList(id, page);
			return ResponseEntity.ok(list);
		} catch (Exception ex) {
			ex.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@DeleteMapping("/reviews")
	public ResponseEntity<String> deleteMyReviews(
			@RequestParam("id") int id,
			@RequestBody List<Integer> deleteReviewList) {
		try {
			mService.deleteMyReviews(id, deleteReviewList);
			return ResponseEntity.ok("OK");
		} catch (Exception ex) {
			ex.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@DeleteMapping("/replies")
	public ResponseEntity<String> deleteMyReviewReplies(
			@RequestParam("id") int id,
			@RequestBody List<Integer> deleteReplyList) {
		try {
			mService.deleteMyReviewReplies(id, deleteReplyList);
			return ResponseEntity.ok("OK");
		} catch (Exception ex) {
			ex.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	
}
