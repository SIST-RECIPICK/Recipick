package com.sist.web.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.sist.web.vo.RecipeSearchVO;

@Mapper
@Repository
public interface RecipeSearchMapper {
/*
 * <select id="selectRecipeSearch" resultType="com.sist.web.vo.RecipeSearchVO">
    SELECT r.RCP_SEQ as rcp_seq,
    	   r.RCP_NM as rcp_nm,
    	   r.ATT_FILE_NO_MAIN as att_file_no_main,
    	   r.INFO_ENG as info_eng,
    	   r.RCP_PAT2 as rcp_pat2,
    	   CASE WHEN b.id IS NOT NULL THEN 1 ELSE 0 END as is_bookmark 
   	FROM RECIPE r
   	LEFT JOIN recipe_bookmark b ON r.RCP_SEQ = b.RCP_SEQ AND b.users_id =#{user_id}
   	WHERE r.RCP_NM LIKE '%'||#{keyword}||'%'
    ORDER BY is_bookmark DESC, r.RCP_SEQ
  </select>
 */
	public List<RecipeSearchVO> selectRecipeSearch(
			@Param("keyword") String keyword,
			@Param("user_id") int user_id
	);
}
