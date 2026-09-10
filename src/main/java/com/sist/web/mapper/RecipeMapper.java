package com.sist.web.mapper;
import java.util.List;
import java.util.Map;

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
	// 기존 int에서 정렬되는 값까지 추가돼 map으로 변경
	public List<RecipeListVO> recipeListData(Map map);
	
	/*
	 * <select id="recipeTotalPage" resultType="int">
		  SELECT COUNT(*) FROM recipe
	   </select>
	 */
	// 레시피 총 페이지 수 구하기
	public int recipeTotalPage();
	
	/*
	 *  <select id="recipeCategoryData" resultType="com.sist.web.vo.RecipeListVO" parameterType="hashmap">
		  SELECT rcp_seq,rcp_nm,rcp_pat2,info_eng,user_id,att_file_no_main,hit,hash_tag,users.nickname,
		   (SELECT COUNT(*) FROM recipe_like 
		                     WHERE recipe_like.recipe_id = recipe.rcp_seq) AS like_count
		  FROM recipe
		  JOIN users
		  ON recipe.user_id = users.id
		  WHERE rcp_pat2 = #{main_category}
		  ORDER BY rcp_seq DESC
		  OFFSET #{start} ROWS FETCH NEXT 12 ROWS ONLY
       </select>
	 */
	// 레시피 특정 카테고리 조회
	public List<RecipeListVO> recipeCategoryData(Map map);
	
	/*
	 *  <select id="categoryTotalPage" resultType="int" parameterType="String">
		  SELECT CEIL(COUNT(*)/12.0) FROM recipe
		  WHERE rcp_pat2 = #{main_category} 
	    </select>
	 */
	// 카테고리 용 총 페이지 
	public int categoryTotalPage(String main_category);
	
	
	/*
	 * <select id="checkRecipeLike" resultType="int" parameterType="hashmap">
	    SELECT COUNT(*) FROM recipe_like
	    WHERE user_id = #{user_id} AND recipe_id = #{recipe_id}
	</select>
	 */
	// 좋아요 여부 확인 (0=안눌렀음, 1=이미 눌렀음)
	public int checkRecipeLike(Map map);

	
	/*
	 * <insert id="insertRecipeLike" parameterType="hashmap">
	    INSERT INTO recipe_like (like_id, user_id, recipe_id)
	    VALUES (recipe_like_seq.NEXTVAL, #{user_id}, #{recipe_id})
	</insert>
	
	 */
	// 좋아요 등록
	public int insertRecipeLike(Map map);

	/*
	 * <delete id="deleteRecipeLike" parameterType="hashmap">
	    DELETE FROM recipe_like
	    WHERE user_id = #{user_id} AND recipe_id = #{recipe_id}
	</delete>
	 */
	
	// 좋아요 취소
	public int deleteRecipeLike(Map map);
}
