package com.sist.web.restcontroller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sist.web.service.AdminService;
import com.sist.web.vo.UsersVO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminRestController {

	private final AdminService adminService;

	@GetMapping("/user/list")
	public ResponseEntity<Map<String, Object>> memberList(@RequestParam("page") int page) {
		Map<String, Object> map = new HashMap<>();
		try {

			List<UsersVO> list = adminService.usersList(page);

			final int BLOCK = 15;

			int curpage = page;
			int totalpage = list.size();
			int startpage = ((curpage - 1) / BLOCK * BLOCK) + 1; // 시작 페이지
			int endpage = ((curpage - 1) / BLOCK * BLOCK) + BLOCK; // 종료 페이지
			if (endpage > totalpage)
				endpage = totalpage;

			map.put("list", list);
			map.put("curpage", curpage);
			map.put("totalpage", totalpage);
			map.put("startpage", startpage);
			map.put("endpage", endpage);

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}

		return ResponseEntity.ok(map);
	}

}
