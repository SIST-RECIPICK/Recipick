package com.sist.web.service;

import java.util.*;

import org.apache.ibatis.annotations.Param;

import com.sist.web.vo.Review_BoardVO;
import com.sist.web.vo.Review_Board_ReplyVO;

public interface ReviewService {
	public List<Review_BoardVO> ReviewBoardListData(int page, String keyword, String type);
    public int reviewBoardTotalpage(String keyword, String type);
    public int[] pages(int page, String keyword, String type);
	public Review_BoardVO boardDetailData(int id);
	public List<Review_BoardVO> writerOtherReviews(Map<String, Object> map);
	public List<Review_BoardVO> recipeOtherReviews(Map<String, Object> map);
	public List<Review_Board_ReplyVO> boardReplyList(int reviewBoardId);
	public void reviewInsert(Review_BoardVO vo);
	public void reviewUpdate(Review_BoardVO vo);
	public void reviewDelete(int id);
	public void reviewReplyInsert(Review_Board_ReplyVO vo);
	public void reviewReplyDelete(int id);
}
