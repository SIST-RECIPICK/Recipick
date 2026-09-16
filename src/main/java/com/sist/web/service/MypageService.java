package com.sist.web.service;

import java.util.*;

import org.springframework.web.multipart.MultipartFile;

import com.sist.web.vo.*;

public interface MypageService {

	public UsersVO mypageProfile(int id);

    public Map<String, Object> mypageMainCount(int id);
    
    public void updateMyProfile(int userId, String nickname, MultipartFile file);
    public void changePassword(int userId, String currentPassword, String newPassword, String newPasswordConfirm);

    public List<Review_BoardVO> myReviewList(int id, int page);
    public int myReviewTotalPage(int id);
    public List<Review_Board_ReplyVO> myReviewReplyList(int id, int page);
    public int myReviewReplyTotalPage(int id);

    public void deleteMyReview(int userId, int reviewId);
    public void deleteMyReviewReplies(int id, List<Integer> deleteReplyList);
}