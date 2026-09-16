package com.sist.web.restcontroller;

import java.util.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sist.web.security.JwtUser;
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
	public ResponseEntity<?> mypageProfile(@AuthenticationPrincipal JwtUser jwtUser) {
	    if (jwtUser == null) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	    }
	    UsersVO vo = mService.mypageProfile(jwtUser.getUserId());

	    return ResponseEntity.ok(vo);
	}

	@GetMapping("/main_count")
	public ResponseEntity<Map<String, Object>> mypageMainCount(@AuthenticationPrincipal JwtUser jwtUser) {
		if (jwtUser == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		return ResponseEntity.ok(mService.mypageMainCount(jwtUser.getUserId()));
	}
	
	@PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> updateMyProfile(
	        @RequestParam(value = "nickname", required = false) String nickname,
	        @RequestPart(value = "file", required = false) MultipartFile file,
	        @AuthenticationPrincipal JwtUser jwtUser) {

	    if (jwtUser == null) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body("로그인이 필요합니다.");
	    }

	    try {
	        mService.updateMyProfile(jwtUser.getUserId(), nickname, file);
	        return ResponseEntity.ok("OK");
	    } catch (IllegalArgumentException ex) {
	        return ResponseEntity.badRequest().body(ex.getMessage());
	    } catch (Exception ex) {
	        ex.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body("프로필 수정에 실패했습니다.");
	    }
	}
	
	@PutMapping("/password")
	public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request,
			@AuthenticationPrincipal JwtUser jwtUser) {
		if (jwtUser == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
		}
		try {
			String currentPassword = request.get("currentPassword");
			String newPassword = request.get("newPassword");
			String newPasswordConfirm = request.get("newPasswordConfirm");
			mService.changePassword(jwtUser.getUserId(), currentPassword, newPassword, newPasswordConfirm);
			return ResponseEntity.ok("OK");
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("비밀번호 변경에 실패했습니다.");
		}
	}
	
	@GetMapping("/reviews")
	public ResponseEntity<Map<String, Object>> myReviewList(
	        @RequestParam(value = "page", defaultValue = "1") int page,
	        @AuthenticationPrincipal JwtUser jwtUser) {
		
		if (jwtUser == null) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	    }

	    try {
	        int id = jwtUser.getUserId();

	        List<Review_BoardVO> list = mService.myReviewList(id, page);
	        int totalpage = mService.myReviewTotalPage(id);

	        Map<String, Object> result = new HashMap<>();
	        result.put("list", list);
	        result.put("totalpage", totalpage);
	        result.put("curpage", page);

	        return ResponseEntity.ok(result);

	    } catch (Exception ex) {
	        ex.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	    }
	}

	@GetMapping("/replies")
	public ResponseEntity<Map<String, Object>> myReviewReplyList(
			@RequestParam(value = "page", defaultValue = "1") int page, @AuthenticationPrincipal JwtUser jwtUser) {
		if (jwtUser == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		int id = jwtUser.getUserId();
		List<Review_Board_ReplyVO> list = mService.myReviewReplyList(id, page);
		
		int totalpage = mService.myReviewReplyTotalPage(id);
		int block = 10;
		
		int startpage = ((page - 1) / block) * block + 1;
		int endpage = Math.min(startpage + block - 1, totalpage);
		
		Map<String, Object> result = new HashMap<>();

		result.put("list", list);
		result.put("pages", Arrays.asList(page, totalpage, startpage, endpage));

		return ResponseEntity.ok(result);
	}


	@DeleteMapping("/reviews/{reviewId}")
	public ResponseEntity<String> deleteMyReview(
	        @PathVariable("reviewId") int reviewId,
	        @AuthenticationPrincipal JwtUser jwtUser) {
	    if (jwtUser == null) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body("로그인이 필요합니다.");
	    }
	    mService.deleteMyReview(jwtUser.getUserId(), reviewId);

	    return ResponseEntity.ok("OK");
	}

	@DeleteMapping("/replies")
	public ResponseEntity<String> deleteMyReviewReplies(@RequestBody List<Integer> deleteReplyList,
			@AuthenticationPrincipal JwtUser jwtUser) {
		if (jwtUser == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
		}
		mService.deleteMyReviewReplies(jwtUser.getUserId(), deleteReplyList);

		return ResponseEntity.ok("OK");
	}

	
	
}
