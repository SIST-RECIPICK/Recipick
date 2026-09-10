package com.sist.web.restcontroller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sist.web.service.CommunityService;
import com.sist.web.vo.CurationVO;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/community")
@CrossOrigin(origins = "*")
public class CommunityRestController {
	
	private final CommunityService communityService;
	
	@GetMapping("/curation")
	public ResponseEntity<Map<String, Object>> curation_list(
			@RequestParam(value = "page", defaultValue = "1") int page) {

		Map<String, Object> map = new HashMap<>();
		try {
			List<CurationVO> list = communityService.curation_list(page);
			int[] pages = communityService.pages(page);

			map.put("list", list);
			map.put("curpage", pages[0]);
			map.put("totalpage", pages[1]);
			map.put("startpage", pages[2]);
			map.put("endpage", pages[3]);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		return ResponseEntity.ok(map);
	}
	
	@GetMapping("/curation/{id}")
	public ResponseEntity<?> curation_detail(@PathVariable("id") int id) {
		CurationVO curation = null;
		try {
			curation = communityService.selectCurationDetail(id);

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}

		return ResponseEntity.ok(curation);
	}
	
}
