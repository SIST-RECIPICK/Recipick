package com.sist.web.mapper;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.sist.web.vo.*;
@Mapper
@Repository
public interface RecipeMapper {

	/*
	 *   <select id="RecipeListData" resultType="com.sist.web.vo.RecipeVO" parameterType="int">
		  SELECT rcp_seq,rcp_nm,rcp_pat2,info_eng,user_id,private String att_file_no_main,hit
		  FROM recipe
		  WHERE rcp_seq=#{rcp_seq}
		 </select>
	 */
	// 목록 출력
	public List<RecipeVO> recipeListData(int start);
}
