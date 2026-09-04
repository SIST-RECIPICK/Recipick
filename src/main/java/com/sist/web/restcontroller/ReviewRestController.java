package com.sist.web.restcontroller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sist.web.service.ReviewService;
import com.sist.web.vo.Review_BoardVO;

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
            Map<String, Object> map = rService.ReviewBoardListData(page);
            return ResponseEntity.ok(map);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
