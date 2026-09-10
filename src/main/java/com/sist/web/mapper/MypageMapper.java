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

    public UsersVO mypageSideProfile(@Param("userId") int userId);

    public Map<String, Object> mypageMainCount(@Param("userId") int userId);
    
    public List<Review_BoardVO> myReviewList(@Param("userId") int userId, @Param("start") int start);    
    public List<Review_Board_ReplyVO> myReviewReplyList(@Param("userId") int userId, @Param("start") int start);
    public void deleteMyReviews(@Param("userId") int userId, @Param("idList") List<Integer> idList);
    public void deleteMyReviewReplies(@Param("userId") int userId, @Param("idList") List<Integer> idList);
    
}