package com.sist.web.service;

import java.util.*;
import org.springframework.stereotype.Service;

import com.sist.web.mapper.MypageMapper;
import com.sist.web.vo.*;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MypageServiceImpl implements MypageService {

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
    public List<Review_Board_ReplyVO> myReviewReplyList(int id, int page) {
        int start = (page - 1) * ROW_SIZE;
        return mMapper.myReviewReplyList(id, start);
    }

    @Override
    /*
     * 일괄 삭제를 위해 추가적으로 무언가가 필요
     * 
     * 1.
     * @Transactional으로 댓글삭제와 글 삭제를 일괄로 처리
     * 2.
     * ON DELETE CASCADE로 글 삭제시 관련 댓글도 같이 삭제되게 테이블 설계
     * 3.
     * 글은 하나씩만 삭제하도록 계획을 수정
     * 4.
     * Review 테이블에 status 컬럼을 추가하여 글 삭제가 데이터베이스 삭제가 아니라 데이터베이스에서 삭제 상태를 
     * delete, live로 설정하도록 하는 방법
     * 
     * 로그인 기능이 아직 미구현이니, 여기까지만 만들어두고 나중에 수정
     */
    public void deleteMyReviews(int id, List<Integer> deleteReviewList) {
        if (deleteReviewList != null && !deleteReviewList.isEmpty()) {
            mMapper.deleteMyReviews(id, deleteReviewList);
        }
    }

    @Override
    public void deleteMyReviewReplies(int id, List<Integer> deleteReplyList) {
        if (deleteReplyList != null && !deleteReplyList.isEmpty()) {
            mMapper.deleteMyReviewReplies(id, deleteReplyList);
        }
    }

	
}