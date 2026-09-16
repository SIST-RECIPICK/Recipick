package com.sist.web.service;

import com.sist.web.mapper.ReviewMapper;
import com.sist.web.util.CloudinaryUtil;

import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.sist.web.mapper.MypageMapper;
import com.sist.web.vo.*;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MypageServiceImpl implements MypageService {

	private final ReviewMapper reviewMapper;
	private final MypageMapper mMapper;
	private final CloudinaryUtil cloudinaryUtil;
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
	public void updateMyProfile(int userId, String nickname, MultipartFile file) {
		if (nickname == null || nickname.isBlank()) {
			throw new IllegalArgumentException("닉네임을 입력해주세요.");
		}
		if (!nickname.matches("^[가-힣a-zA-Z0-9]{2,10}$")) {
			throw new IllegalArgumentException("닉네임은 한글, 영문, 숫자를 사용하여 2~10자로 입력해주세요.");
		}
		String profileImageUrl = null;
		if (file != null && !file.isEmpty()) {
			if (file.getSize() > 5 * 1024 * 1024) {
				throw new IllegalArgumentException("프로필 이미지는 5MB 이하만 가능합니다.");
			}
			String contentType = file.getContentType();
			if (!"image/jpeg".equals(contentType) && !"image/png".equals(contentType)) {
				throw new IllegalArgumentException("JPG 또는 PNG 이미지만 업로드할 수 있습니다.");
			}
			try {
				Map<String, Object> uploadResult = cloudinaryUtil.uploadImage(file);
				profileImageUrl = (String) uploadResult.get("url");
			} catch (Exception ex) {
				throw new RuntimeException("프로필 이미지 업로드에 실패했습니다.", ex);
			}
		}
		mMapper.updateMyProfile(userId, nickname, profileImageUrl);
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