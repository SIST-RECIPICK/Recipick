package com.sist.web.restcontroller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sist.web.service.ReviewService;
import com.sist.web.vo.Review_BoardVO;
import com.sist.web.vo.Review_Board_ReplyVO;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ReviewRestController {

	private final ReviewService rService;

	@GetMapping("/review/list")
	public ResponseEntity<Map<String, Object>> review_list(
			@RequestParam(value = "page", defaultValue = "1") int page
	) {
		try {
			List<Review_BoardVO> list = rService.ReviewBoardListData(page);
			int[] pages = rService.pages(page);

			Map<String, Object> map = new HashMap<>();
			map.put("list", list);
			map.put("curpage", pages[0]);
			map.put("totalpage", pages[1]);
			map.put("startpage", pages[2]);
			map.put("endpage", pages[3]);

			return ResponseEntity.ok(map);
		} catch (Exception ex) {
			ex.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	@GetMapping("/review/detail")
	public ResponseEntity<Map<String, Object>> review_detail(@RequestParam("id") int id) {
		try {
			Review_BoardVO board = rService.boardDetailData(id);
			if (board == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
			}

			List<Review_Board_ReplyVO> replyList = rService.boardReplyList(id);

			Map<String, Object> writerMap = new HashMap<>();
			writerMap.put("users_id", board.getWriter_id());
			writerMap.put("id", id);
			List<Review_BoardVO> writerReviews = rService.writerOtherReviews(writerMap);

			Map<String, Object> recipeMap = new HashMap<>();
			recipeMap.put("rcp_seq", board.getRcp_seq());
			recipeMap.put("id", id);
			List<Review_BoardVO> recipeReviews = rService.recipeOtherReviews(recipeMap);

			Map<String, Object> map = new HashMap<>();
			map.put("board", board);
			map.put("replyList", replyList);
			map.put("writerReviews", writerReviews);
			map.put("recipeReviews", recipeReviews);

			return ResponseEntity.ok(map);
		} catch (Exception ex) {
			ex.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	@PostMapping("/review/insert")
	public ResponseEntity<String> review_insert(@RequestBody Review_BoardVO vo) {
	    try {
	        rService.reviewInsert(vo);
	        return ResponseEntity.ok("OK");
	    } catch (Exception ex) {
	        ex.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	    }
	}
	
	@PutMapping("/review/update")
	public ResponseEntity<String> review_update(@RequestBody Review_BoardVO vo) {
	    try {
	        rService.reviewUpdate(vo);
	        return ResponseEntity.ok("OK");
	    } catch (Exception ex) {
	        ex.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	    }
	}
	
	@DeleteMapping("/review/delete")
	public ResponseEntity<String> review_delete(@RequestParam("id") int id) {
	    try {
	        rService.reviewDelete(id);
	        return ResponseEntity.ok("OK");
	    } catch (Exception ex) {
	        ex.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	    }
	}
	
	@PostMapping("/review/reply/insert")
	public ResponseEntity<String> review_reply_insert(@RequestBody Review_Board_ReplyVO vo) {
	    try {
	        rService.reviewReplyInsert(vo);
	        return ResponseEntity.ok("OK");
	    } catch (Exception ex) {
	        ex.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	    }
	}
	
	@DeleteMapping("/review/reply/delete")
	public ResponseEntity<String> review_reply_delete(@RequestParam("id") int id) {
	    try {
	        rService.reviewReplyDelete(id);
	        return ResponseEntity.ok("OK");
	    } catch (Exception ex) {
	        ex.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	    }
	}
	
}