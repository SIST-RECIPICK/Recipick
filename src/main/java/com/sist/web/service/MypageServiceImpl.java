package com.sist.web.service;

import com.sist.web.mapper.ReviewMapper;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sist.web.mapper.MypageMapper;
import com.sist.web.vo.*;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MypageServiceImpl implements MypageService {

	private final ReviewMapper reviewMapper;
	private final MypageMapper mMapper;
    private final int ROW_SIZE = 10;

    @Override
    public UsersVO mypageProfile(int id) {
        return mMapper.mypageProfile(id);
    }

    @Override
    public Map<String, Object> mypageMainCount(int id) {
        return mMapper.mypageMainCount(id);
    }

    @Override
    public List<Review_BoardVO> myReviewList(int id, int page) {
        int start = (page - 1) * ROW_SIZE;
        return mMapper.myReviewList(id, start);
    }
    
    @Override
    public int myReviewTotalPage(int id) {
        return mMapper.myReviewTotalPage(id);
    }

    @Override
    public List<Review_Board_ReplyVO> myReviewReplyList(int id, int page) {
        int start = (page - 1) * ROW_SIZE;
        return mMapper.myReviewReplyList(id, start);
    }
    
    @Override
    public int myReviewReplyTotalPage(int id) {
        return mMapper.myReviewReplyTotalPage(id);
    }

    @Transactional
    @Override
    public void deleteMyReview(int userId, int reviewId) {
        reviewMapper.reviewReplyAllDelete(reviewId);
        reviewMapper.reviewDelete(reviewId, userId);
    }

    @Override
    public void deleteMyReviewReplies(int id, List<Integer> deleteReplyList) {
        if (deleteReplyList != null && !deleteReplyList.isEmpty()) {
            mMapper.deleteMyReviewReplies(id, deleteReplyList);
        }
    }

	
}