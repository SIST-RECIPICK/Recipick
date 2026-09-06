package com.sist.web.service;

import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
	public List<Review_BoardVO> ReviewBoardListData(int page) {
		int start = (page - 1) * ROW_SIZE;
		return rMapper.ReviewBoardListData(start);
	}

	@Override
	public int reviewBoardTotalpage() {
		return rMapper.reviewBoardTotalpage();
	}

	@Override
	public int[] pages(int page) {
		int totalpage = rMapper.reviewBoardTotalpage();
		final int BLOCK = 10;

		int startpage = ((page - 1) / BLOCK * BLOCK) + 1;
		int endpage = ((page - 1) / BLOCK * BLOCK) + BLOCK;
		if (endpage > totalpage) {
			endpage = totalpage;
		}

		int[] pages = { page, totalpage, startpage, endpage };
		return pages;
	}

	@Override
	public Review_BoardVO boardDetailData(int id) {
	    rMapper.boardHitUpdate(id);
	    return rMapper.boardDetailData(id);
	}
	@Override
	public List<Review_BoardVO> writerOtherReviews(Map<String, Object> map) {
		return rMapper.writerOtherReviews(map);
	}

	@Override
	public List<Review_BoardVO> recipeOtherReviews(Map<String, Object> map) {
		return rMapper.recipeOtherReviews(map);
	}

	@Override
	public List<Review_Board_ReplyVO> boardReplyList(int reviewBoardId) {
		return rMapper.boardReplyList(reviewBoardId);
	}
	
	@Override
	public void reviewInsert(Review_BoardVO vo) {
	    rMapper.reviewInsert(vo);
	}
	
	@Override
	public void reviewUpdate(Review_BoardVO vo) {
	    rMapper.reviewUpdate(vo);
	}
	
	@Override
	@Transactional
	public void reviewDelete(int id) {
	    rMapper.reviewReplyAllDelete(id);
	    rMapper.reviewDelete(id);
	}
	
	@Override
	public void reviewReplyInsert(Review_Board_ReplyVO vo) {
	    rMapper.reviewReplyInsert(vo);
	}

	@Override
	public void reviewReplyDelete(int id) {
	    rMapper.reviewReplyDelete(id);
	}

}
