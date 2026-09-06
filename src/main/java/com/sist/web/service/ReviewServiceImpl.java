package com.sist.web.service;

import java.util.*;

import org.springframework.stereotype.Service;

import com.sist.web.mapper.ReviewMapper;
import com.sist.web.vo.Review_BoardVO;
import com.sist.web.vo.Review_Board_ReplyVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
	private final ReviewMapper rMapper;
	private final int ROW_SIZE = 12;
	
	@Override
	public List<Review_BoardVO> ReviewBoardListData(int start) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public int reviewBoardTotalpage() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public Review_BoardVO boardDetailData(int id) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public List<Review_BoardVO> writerOtherReviews(Map<String, Object> map) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public List<Review_BoardVO> recipeOtherReviews(Map<String, Object> map) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public List<Review_Board_ReplyVO> boardReplyList(int reviewBoardId) {
		// TODO Auto-generated method stub
		return null;
	}

	

}
