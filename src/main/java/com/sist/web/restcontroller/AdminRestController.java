package com.sist.web.restcontroller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Delete;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sist.web.service.AdminService;
import com.sist.web.vo.CurationVO;
import com.sist.web.vo.UsersVO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminRestController {

	private final AdminService adminService;

	@GetMapping("/user")
	public ResponseEntity<Map<String, Object>> memberList(@RequestParam(value = "page", defaultValue = "1") int page) {
		Map<String, Object> map = new HashMap<>();
		try {

			List<UsersVO> list = adminService.usersList(page);
			int[] pages = adminService.pages(page, "users");

			map.put("list", list);
			map.put("curpage", pages[0]);
			map.put("totalpage", pages[1]);
			map.put("startpage", pages[2]);
			map.put("endpage", pages[3]);

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}

		return ResponseEntity.ok(map);
	}

	// RequestBody는 하나의 body 덩어리로만 받을 수 있음
	@PutMapping("/user/role")
	public ResponseEntity<?> role_update(@RequestBody UsersVO vo) {
		try {
			adminService.userRoleUpdate(vo.getId(), vo.getRole());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}

		return ResponseEntity.ok().build();
	}

	@PutMapping("/user/status")
	public ResponseEntity<?> status_update(@RequestBody UsersVO vo) {
		try {
			adminService.userStatusUpdate(vo.getId(), vo.getStatus());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		return ResponseEntity.ok().build();
	}

	@GetMapping("/curation")
	public ResponseEntity<Map<String, Object>> curation_list(
			@RequestParam(value = "page", defaultValue = "1") int page) {

		Map<String, Object> map = new HashMap<>();
		try {
			List<CurationVO> list = adminService.curation_list(page);
			int[] pages = adminService.pages(page, "curation");

			map.put("list", list);
			map.put("curpage", pages[0]);
			map.put("totalpage", pages[1]);
			map.put("startpage", pages[2]);
			map.put("endpage", pages[3]);

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		return ResponseEntity.ok(map);
	}

	@GetMapping("/curation/{id}")
	public ResponseEntity<?> curation_detail(@PathVariable("id") int id) {
		CurationVO curation = null;
		try {
			curation = adminService.selectCurationDetail(id);

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}

		return ResponseEntity.ok(curation);
	}
	
	@DeleteMapping("/curation/{id}")
	public ResponseEntity<?> curation_delete(@PathVariable("id") int id){
		
		try {
			adminService.deleteCuration(id);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		return ResponseEntity.ok().build();
	}
}
