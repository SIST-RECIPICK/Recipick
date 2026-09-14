package com.sist.web.mapper;

import java.util.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.sist.web.vo.Review_BoardVO;
import com.sist.web.vo.Review_Board_ReplyVO;
import com.sist.web.vo.UsersVO;

@Mapper
@Repository
public interface MypageMapper {

    public UsersVO mypageProfile(@Param("id") int id);

    public Map<String, Object> mypageMainCount(@Param("id") int id);
    
    public List<Review_BoardVO> myReviewList(@Param("id") int id, @Param("start") int start);    
    public List<Review_Board_ReplyVO> myReviewReplyList(@Param("id") int id, @Param("start") int start);

    public void deleteMyReviews(@Param("id") int id, @Param("deleteReviewList") List<Integer> deleteReviewList);
    public void deleteMyReviewReplies(@Param("id") int id, @Param("deleteReplyList") List<Integer> deleteReplyList);
    
}