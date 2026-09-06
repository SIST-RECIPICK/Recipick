package com.sist.web.mapper;

import java.util.*;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.sist.web.vo.Review_BoardVO;
import com.sist.web.vo.Review_Board_ReplyVO;

@Mapper
@Repository
public interface ReviewMapper {
	public List<Review_BoardVO> ReviewBoardListData(@Param("start") int start);
	public int reviewBoardTotalpage();
	public Review_BoardVO boardDetailData(@Param("id") int id);
	public List<Review_BoardVO> writerOtherReviews(Map<String, Object> map);
	public List<Review_BoardVO> recipeOtherReviews(Map<String, Object> map);
	public List<Review_Board_ReplyVO> boardReplyList(@Param("review_board_id") int reviewBoardId);
	public void boardHitUpdate(@Param("id") int id);
	public void reviewInsert(Review_BoardVO vo);
	public void reviewUpdate(Review_BoardVO vo);
	public void reviewReplyAllDelete(@Param("review_board_id") int reviewBoardId);
	public void reviewDelete(@Param("id") int id);
	public void reviewReplyInsert(Review_Board_ReplyVO vo);
	public void reviewReplyDelete(@Param("id") int id);
	
}
