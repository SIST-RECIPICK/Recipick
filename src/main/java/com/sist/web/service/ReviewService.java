package com.sist.web.service;

import java.util.List;

import com.sist.web.vo.Review_BoardVO;

public interface ReviewService {
	public List<Review_BoardVO> ReviewBoardListData(int page);
}
