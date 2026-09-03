package com.sist.web.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sist.web.mapper.ReviewMapper;
import com.sist.web.vo.Review_BoardVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
	private final ReviewMapper rMapper;

	@Override
	public List<Review_BoardVO> ReviewBoardListData(int page) {

		return rMapper.ReviewBoardListData(page);
	}

}
