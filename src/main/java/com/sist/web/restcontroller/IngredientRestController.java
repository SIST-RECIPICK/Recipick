package com.sist.web.restcontroller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sist.web.service.IngredientService;
import com.sist.web.vo.IngredientVO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/ingredients")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class IngredientRestController {
	private final IngredientService iService;

	@GetMapping("/list")
	public List<IngredientVO> searchData(@RequestParam("keyword") String keyword) {
		//System.out.println("keyword :: " + keyword);
		return iService.searchData(keyword);
	}
}
