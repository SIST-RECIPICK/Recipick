package com.sist.web.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.sist.web.vo.IngredientUnitVO;
import com.sist.web.vo.MyListVO;
import com.sist.web.vo.RecipeLikeVO;
import com.sist.web.vo.RecipeManualVO;
import com.sist.web.vo.RecipeVO;
import com.sist.web.vo.Review_BoardVO;

@Mapper
@Repository
public interface RecipeDetailMapper {
	
	//레시피 상세 데이터
	/*
	<select id="recipeDetailData" resultType="com.sist.web.vo.RecipeVO" parameterType="int">
		SELECT rcp_seq,rcp_nm,rcp_way2,rcp_pat2,info_wgt,info_eng,info_car,info_pro,info_fat,
		info_na,hash_tag,att_file_no_main,att_file_no_main,att_file_no_mk,rcp_parts_dtls,rcp_na_tip
		FROM recipe
		WHERE rcp_seq=#{rcp_seq}
	</select>
	 */
	public RecipeVO recipeDetailData(int rcp_seq);
	
	//레시피 상세 데이터 = 제조법
	/*
	<select id="recipeHowList" resultType="com.sist.web.vo.RecipeManualVO" parameterType="int">
		SELECT rcp_seq,step_no,manual_desc,manual_img
		FROM recipemanual
		WHERE rcp_seq=#{rcp_seq}
	</select>
	 */
	public List<RecipeManualVO> recipeHowList(int rcp_seq);
	
	//레시피 상세 세이터 = 재료
	/*
	<select id="ingredientUnitList" resultType="com.sist.web.vo.IngredientUnitVO" parameterType="int">
		SELECT recipe_id,name,amount,unit,amount_text,original
		FROM Ingredient_unit
		WHERE rcp_seq=#{rcp_seq}
	</select>
	 */
	public List<IngredientUnitVO> ingredientUnitList(int rcp_seq);
	
	//레시피 좋아요 유무
	/*
	<select id="recipeDetailLikeExist" resultType="int" parameterType="com.sist.web.vo.RecipeLikeVO">
		SELECT COUNT(*)
		FROM recipe_like
		WHERE recipe_id=#{recipe_id} AND user_id=#{user_id}
	</select>
	*/
	public int recipeDetailLikeExist(@Param("recipe_id") int recipe_id,@Param("user_id") int user_id);
	
	//레시피 좋아요
	/*
	<insert id="recipeDetailLikeInsert" parameterType="com.sist.web.vo.RecipeLikeVO">
		INSERT INTO recipe_like(like_id,recipe_id,user_id) 
		VALUES(recipe_like_seq.nextval,#{recipe_id},#{user_id})
	</insert>
	*/
	public void recipeDetailLikeInsert(@Param("recipe_id") int recipe_id,@Param("user_id") int user_id);
	
	//레시피 좋아요 취소
	/*
	<delete id="recipeDetailLikeDelete" parameterType="com.sist.web.vo.RecipeLikeVO">
		DELETE FROM recipe_like
		WHERE recipe_id=#{recipe_id} AND user_id=#{user_id}
	</delete>
	*/
	public void recipeDetailLikeDelete(@Param("recipe_id") int recipe_id,@Param("user_id") int user_id);
	
	//레시피 북마크 유무
	/*
	<select id="recipeDetailBookmarkExist" resultType="int" parameterType="com.sist.web.vo.RecipeBookMarkVO">
		SELECT COUNT(*)
		FROM recipe_bookmark
		WHERE recipe_id=#{recipe_id} AND user_id=#{user_id}
	</select>
	*/
	public int recipeDetailBookmarkExist(@Param("recipe_id") int recipe_id,@Param("user_id") int user_id);
	
	//레시피 북마크 추가
	/*
	<insert id="recipeDetailBookmarkInsert" parameterType="com.sist.web.vo.RecipeBookMarkVO">
		INSERT INTO recipe_bookmark(bookmark_id,recipe_id,user_id) 
		VALUES(recipe_bm_seq.nextval,#{recipe_id},#{user_id})
	</insert>
	*/
	public void recipeDetailBookmarkInsert(@Param("recipe_id") int recipe_id,@Param("user_id") int user_id);
	
	//레시피 북마크 취소
	/*
	<delete id="recipeDetailBookmarkDelete" parameterType="com.sist.web.vo.RecipeBookMarkVO">
		DELETE FROM recipe_bookmark
		WHERE recipe_id=#{recipe_id} AND user_id=#{user_id}
	</delete>
	*/
	public void recipeDetailBookmarkDelete(@Param("recipe_id") int recipe_id,@Param("user_id") int user_id);
	
	//레시피 좋아요 목록
	/*
	<select id="userLikeList" resultType="com.sist.web.vo.MyListVO" parameterType="int">
		SELECT rcp_seq,rcp_nm,att_file_no_main,hit,nickname,
			(
        		SELECT COUNT(*)
        		FROM recipe_like r
        		WHERE rl.recipe_id = rcp_seq
		    ) AS count
		FROM recipe r
		join recipe_like l
		on r.rcp_seq = l.recipe_id 
		join users u
		on r.user_id = u.id
		WHERE l.user_id = #{user_id}
		ORDER BY l.create_at DESC
		OFFSET #{start} ROWS FETCH NEXT 12 ROWS ONLY
	</select>
	 */
	public List<MyListVO> userLikeList(@Param("user_id")int user_id,@Param("start") int start);
	
	//레시피 좋아요 개수
	/*
	<select id="userLikeListCount" resultType="int" parameterType="int">
		SELECT COUNT(*) FROM recipe_like
		WHERE user_id = #{user_id}
	</select>
	 */
	public int userLikeListCount(int user_id);
	
	//북마크 리스트
	/*
	<select id="userMarkList" resultType="com.sist.web.vo.MyListVO" parameterType="int">
		SELECT rcp_seq,rcp_nm,att_file_no_main,hit,nickname
		FROM recipe r
		join recipe_bookmark l
		on r.rcp_seq = l.recipe_id 
		join users u
		on r.user_id = u.id
		WHERE l.user_id = #{user_id}
		ORDER BY l.create_at DESC
		OFFSET #{start} ROWS FETCH NEXT 12 ROWS ONLY
	</select>
	*/
	public List<MyListVO> userMarkList(@Param("user_id")int user_id,@Param("start") int start);
	
	//북마크 리스트 총 갯수
	/*
	<select id="userMarkListCount" resultType="int" parameterType="int">
		SELECT COUNT(*) FROM recipe_bookmark
		WHERE user_id = #{user_id}
	</select>
	*/
	public int userMarkListCount(int user_id);
	
	//연관 레시피 3개 출력
	/*
	<select id="relationRecipeIdList" resultType="com.sist.web.vo.IngredientUnitVO" parameterType="int">
		SELECT recipe_id
		FROM ingredient_unit
		WHERE recipe_id != #{recipe_id}
	    AND name 
	    IN (
		     SELECT name
		     FROM ingredient_unit
		     WHERE recipe_id = #{recipe_id}
		)
		GROUP BY recipe_id
		ORDER BY COUNT(DISTINCT name) DESC
		OFFSET 0 ROWS FETCH NEXT 3 ROWS ONLY
	</select>
	 */
	public List<IngredientUnitVO> relationRecipeIdList(int recipe_id);
	
	//레시피 리뷰
	/*
	 <select id="recipeReviewList" resultType="com.sist.web.vo.Review_BoardVO" parameterType="int">
		SELECT id,users_id,subject,content,image_url,rcp_seq,created_at
		FROM review_board
		WHERE rcp_seq=#{rcp_seq}
		ORDER BY created_at DESC
	 </select>
	 */
	public List<Review_BoardVO> recipeReviewList(int rcp_seq);
}
