package com.sist.web.restcontroller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sist.web.service.RefridgeService;
import com.sist.web.vo.RefridgeVO;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/refrige")
@CrossOrigin(origins = "http://localhost:5173")
public class RefridgeRestController {
	  private final RefridgeService rfService;
	  
	  @PostMapping("/register")
	  public void register(@RequestBody List<RefridgeVO> volist)
	  {
		  rfService.registerData(volist);
	  }
	  
	  @GetMapping("/fridgedata")
	  public List<RefridgeVO> fridgedata(@RequestParam("user_id") int user_id)
	  {
		  return rfService.fridgeData(user_id);
	  }
}
