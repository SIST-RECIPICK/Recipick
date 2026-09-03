package com.sist.web.restcontroller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import com.sist.web.service.ReviewService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ReviewRestController {
	private final ReviewService rService;

	
}
