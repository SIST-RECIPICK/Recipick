package com.sist.web.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.sist.web.vo.Review_BoardVO;

@Mapper
@Repository
public interface ReviewMapper {
	/*
	 * <select id="boardListData" parameterType="int" resultType="Review_BoardVO">
	 * SELECT id, users_id, subject, created_at, hit, image_url, rcp_seq FROM
	 * review_board <!-- Join해서 users_id에서 닉네임 가져와야 함. recipe에서 recipe랑 chef도 가져와야
	 * 함. 좋아요도 join으로 가져와야 함--> </select>
	 */
	public List<Review_BoardVO> ReviewBoardListData(
			@Param("page") int page
	);
	public int reviewBoardTotalpage();
}
