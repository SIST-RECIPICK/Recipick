package com.sist.web.service;

import java.util.*;
import com.sist.web.vo.*;

public interface MypageService {

	public UsersVO mypageProfile(int id);

    public Map<String, Object> mypageMainCount(int id);

    public List<Review_BoardVO> myReviewList(int id, int page);
    public int myReviewTotalPage(int id);
    public List<Review_Board_ReplyVO> myReviewReplyList(int id, int page);

    public void deleteMyReview(int userId, int reviewId);
    public void deleteMyReviewReplies(int id, List<Integer> deleteReplyList);
}