package com.sist.web.restcontroller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sist.web.security.JwtUser;
import com.sist.web.service.ReviewService;
import com.sist.web.util.CloudinaryUtil;
import com.sist.web.vo.RecipeListVO;
import com.sist.web.vo.Review_BoardVO;
import com.sist.web.vo.Review_Board_ReplyVO;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ReviewRestController {

	private final ReviewService rService;
	private final CloudinaryUtil cloudinaryUtil;
	@GetMapping("/review/list")
	public ResponseEntity<Map<String, Object>> review_list(@RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "type", defaultValue = "subject") String type
	) {
		List<Review_BoardVO> list = rService.ReviewBoardListData(page, keyword, type);
		int[] pages = rService.pages(page, keyword, type);

		Map<String, Object> map = new HashMap<>();
		map.put("list", list);
		map.put("curpage", pages[0]);
		map.put("totalpage", pages[1]);
		map.put("startpage", pages[2]);
		map.put("endpage", pages[3]);

		return ResponseEntity.ok(map);
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

	@PostMapping(value = "/review/insert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> review_insert(@RequestPart("board") Review_BoardVO vo,
			@RequestPart(value = "file", required = false) MultipartFile file,
			@AuthenticationPrincipal JwtUser jwtUser) {

		try {
			if (jwtUser == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
			}
			vo.setUsers_id(jwtUser.getUserId());

			if (file != null && !file.isEmpty()) {
				Map<String, Object> uploadResult = cloudinaryUtil.uploadImage(file);

				vo.setImage_url((String) uploadResult.get("url"));
				vo.setImage_size((Double) uploadResult.get("size"));
			}
			rService.reviewInsert(vo);

			return ResponseEntity.ok("OK");

		} catch (IllegalArgumentException ex) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	@GetMapping("/review/recipe/search")
	public ResponseEntity<Map<String, Object>> review_recipe_search(
	        @RequestParam("keyword") String keyword,
	        @RequestParam(value = "page", defaultValue = "1") int page) {

	    try {
	        List<RecipeListVO> list = rService.reviewRecipeSearch(keyword, page);
	        int[] pages = rService.recipePages(page, keyword);
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

	@PutMapping(value = "/review/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> review_update(@RequestPart("board") Review_BoardVO vo,
			@RequestPart(value = "file", required = false) MultipartFile file,
			@AuthenticationPrincipal JwtUser jwtUser) {
		try {
			if (jwtUser == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
			}
			vo.setUsers_id(jwtUser.getUserId());
			if (file != null && !file.isEmpty()) {
				Map<String, Object> uploadResult = cloudinaryUtil.uploadImage(file);
				vo.setImage_url((String) uploadResult.get("url"));
				vo.setImage_size((Double) uploadResult.get("size"));
			}
			rService.reviewUpdate(vo, jwtUser.getUserId());
			return ResponseEntity.ok("OK");
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@DeleteMapping("/review/delete")
	public ResponseEntity<String> review_delete(@RequestParam("id") int id, @AuthenticationPrincipal JwtUser jwtUser) {
		try {
			if (jwtUser == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
			}
			if ("ADMIN".equalsIgnoreCase(jwtUser.getRole())) {
				rService.reviewDeleteAdmin(id);
			} else {
				rService.reviewDelete(id, jwtUser.getUserId());
			}
			return ResponseEntity.ok("OK");
		} catch (Exception ex) {
			ex.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PostMapping("/review/reply/insert")
	public ResponseEntity<String> review_reply_insert(@RequestBody Review_Board_ReplyVO vo,
			@AuthenticationPrincipal JwtUser jwtUser) {
		try {
			if (jwtUser == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
			}
			vo.setUsers_id(jwtUser.getUserId());
			rService.reviewReplyInsert(vo);
			
			return ResponseEntity.ok("OK");
		} catch (Exception ex) {
			ex.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@DeleteMapping("/review/reply/delete")
	public ResponseEntity<String> review_reply_delete(
	        @RequestParam("id") int id,
	        @AuthenticationPrincipal JwtUser jwtUser) {

	    try {
	        if (jwtUser == null) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                    .body("로그인이 필요합니다.");
	        }

	        if ("ADMIN".equalsIgnoreCase(jwtUser.getRole())) {
	            rService.reviewReplyAdminDelete(id);
	        } else {
	            rService.reviewReplyDelete(id, jwtUser.getUserId());
	        }

	        return ResponseEntity.ok("OK");

	    } catch (Exception ex) {
	        ex.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	    }
	}

}