package com.sist.web.mapper;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.sist.web.vo.*;
@Mapper
@Repository
// 직접 디비에서 데이터를 가져오는 역할
public interface RecipeMapper {

	
	/*
	 * <select id="recipeTotalCount" resultType="int">
		 SELECT COUNT(rcp_nm) FROM recipe
		</select>
	 */
	// 레시피 총 개수 구하기
	public int recipeTotalCount();
	
	
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
	
	
	/*
	 * <select id="recipeSearchData" resultType="com.sist.web.vo.RecipeListVO" parameterType="hashmap">
	  SELECT rcp_seq,rcp_nm,rcp_pat2,info_eng,user_id,att_file_no_main,hit,hash_tag,users.nickname,
	   (SELECT COUNT(*) FROM recipe_like 
	                     WHERE recipe_like.recipe_id = recipe.rcp_seq) AS like_count
	  FROM recipe
	  JOIN users
	  ON recipe.user_id = users.id
	  WHERE (rcp_nm LIKE '%' || #{keyword} || '%'
	     OR hash_tag LIKE '%' || #{keyword} || '%'
	     OR rcp_parts_dtls LIKE '%' || #{keyword} || '%')
	  ORDER BY
	  <choose>
	    <when test="sort == 'hit'">
	        hit DESC
	    </when>
	    <otherwise>
	        rcp_seq DESC
	    </otherwise>
	  </choose>
	  OFFSET #{start} ROWS FETCH NEXT 12 ROWS ONLY
	 </select>
	 */
	// 레시피 키워드 검색
	public List<RecipeListVO> recipeSearchData(Map map);
	
	/*
	 * 
	 <select id="searchTotalPage" resultType="int" parameterType="hashmap">
	  SELECT CEIL(COUNT(*)/12.0) FROM recipe
	  WHERE (rcp_nm LIKE '%' || #{keyword} || '%'
	     OR hash_tag LIKE '%' || #{keyword} || '%'
	     OR rcp_parts_dtls LIKE '%' || #{keyword} || '%')
	 </select>
	 */
	
	// 키워드 검색 총 몇 페이지인지 구하기
	public int searchTotalPage(Map map);
	
	// 화면ui에서 키워드 검색 시 카테고리 이동하면 해당 키워드와 관련된 목록조회 하기 위해
	// 카테고리 + 키워드 통합 조회
	public List<RecipeListVO> recipeFilterData(Map map);
	
	// 카테고리 + 키워드 통합 조회용 총페이지
	public int filterTotalPage(Map map);
	
	/*
	 * <select id="searchTotalCount" resultType="int" parameterType="hashmap">
	  SELECT COUNT(*) FROM recipe
	  WHERE (rcp_nm LIKE '%' || #{keyword} || '%'
	     OR hash_tag LIKE '%' || #{keyword} || '%'
	     OR rcp_parts_dtls LIKE '%' || #{keyword} || '%')
	 </select>
	 */
	// 키워드 검색 결과 총 개수 (totalCount 화면 표시용)
	public int searchTotalCount(Map map);
	
	/*
	 * <select id="filterTotalCount" resultType="int" parameterType="hashmap">
	  SELECT COUNT(*) FROM recipe
	  <where>
	    <if test="main_category != null and main_category != '' and main_category != '전체'">
	        AND rcp_pat2 = #{main_category}
	    </if>
	    <if test="keyword != null and keyword != ''">
	        AND (rcp_nm LIKE '%' || #{keyword} || '%'
	           OR hash_tag LIKE '%' || #{keyword} || '%'
	           OR rcp_parts_dtls LIKE '%' || #{keyword} || '%')
	    </if>
	  </where>
	 </select>
	 */
	// 카테고리+키워드 통합 조회 결과 총 개수 (totalCount 화면 표시용)
	public int filterTotalCount(Map map);
	
	/*
	 *  
	 <select id="myRecipeListData" resultType="com.sist.web.vo.RecipeListVO" parameterType="hashmap">
		  SELECT rcp_seq,rcp_nm,rcp_pat2,info_eng,user_id,att_file_no_main,hit,hash_tag,users.nickname,
		   (SELECT COUNT(*) FROM recipe_like 
		                     WHERE recipe_like.recipe_id = recipe.rcp_seq) AS like_count
		  FROM recipe
		  JOIN users
		  ON recipe.user_id = users.id
		  WHERE user_id=#{user_id}
		  ORDER BY rcp_seq DESC
		  OFFSET #{start} ROWS FETCH NEXT 12 ROWS ONLY
	 </select>
	 */
	// 나의 레시피 목록 조회
	// map 안에는 start, user_id
	public List<RecipeListVO> myRecipeListData(Map map); 
	
	/*
	 * <select id="myTotalPage" resultType="int" parameterType="hashmap">
		 SELECT CEIL(COUNT(*)/12.0) FROM recipe
		 WHERE user_id = #{user_id}
		</select>
	 */
	
	// 나의 레시피 총페이지 수 구하기
	public int myTotalPage(Map map);
	
	
	/*
	 * <select id="myTotalCount" resultType="int" parameterType="hashmap">
		 SELECT COUNT(*) FROM recipe
		 WHERE user_id = #{user_id}
		</select>
	 */
	// 나의 레시피 총 개수 구하기
	public int myTotalCount(Map map);
	
	/*
	 * <insert id="recipeInsert" parameterType="com.sist.web.vo.RecipeInsertVO">
	    <selectKey keyProperty="rcp_seq" order="BEFORE" resultType="int">
	        SELECT RECIPE_SEQ.NEXTVAL FROM DUAL
	    </selectKey>
	    INSERT INTO recipe
	    (rcp_seq, rcp_nm, rcp_way2, rcp_pat2, info_wgt,
	     info_eng, info_car, info_pro, info_fat, info_na,
	     hash_tag, att_file_no_main, att_file_no_mk,
	     rcp_parts_dtls, rcp_na_tip, user_id, hit)
	    VALUES
	    (#{rcp_seq}, #{rcp_nm}, #{rcp_way2}, #{rcp_pat2}, #{info_wgt},
	     #{info_eng}, #{info_car}, #{info_pro}, #{info_fat}, #{info_na},
	     #{hash_tag}, #{att_file_no_main}, #{att_file_no_mk},
	     #{rcp_parts_dtls}, #{rcp_na_tip}, #{user_id}, 0)
	</insert>

	 */
	
	// 레시피 등록 (INSERT 실행하면, 새로 뽑힌 번호가 vo.rcp_seq에 자동으로 채워짐)
	public void recipeInsert(RecipeInsertVO vo);

	
	/*
	 * <insert id="recipeManualInsert" parameterType="com.sist.web.vo.RecipeManualVO">
		    INSERT INTO recipe_manua (rcp_seq, step_no, manual_desc, manual_img)
		    VALUES (#{rcp_seq}, #{step_no}, #{manual_desc}, #{manual_img})
		</insert>
	 */
	// 조리순서 1단계 등록 (여러 단계면 여러 번 호출)
	public void recipeManualInsert(RecipeManualVO vo);
	
	/*
	 * <delete id="deleteRecipe" parameterType="int">
		    DELETE FROM recipe
		    WHERE rcp_seq = #{rcp_seq}
		</delete>
	 */
	// 레시피 삭제
	public int deleteRecipe(int rcp_seq);
	
	// 레시피 삭제 전, 연관된 자식 데이터들을 먼저 지우기 위한 메서드들
	public int deleteRecipeLike(int rcp_seq);
	public int deleteRecipeBookmark(int rcp_seq);
	public int deleteRecipeManual(int rcp_seq);
	
	/*
	 * <update id="recipeUpdate" parameterType="com.sist.web.vo.RecipeInsertVO">
	    UPDATE recipe
	    SET
	        rcp_nm = #{rcp_nm},
	        rcp_way2 = #{rcp_way2},
	        rcp_pat2 = #{rcp_pat2},
	        info_eng = #{info_eng},
	        info_car = #{info_car},
	        info_pro = #{info_pro},
	        info_fat = #{info_fat},
	        info_na = #{info_na},
	        info_wgt = #{info_wgt},
	        hash_tag = #{hash_tag},
	        rcp_parts_dtls = #{rcp_parts_dtls},
	        rcp_na_tip = #{rcp_na_tip}
	        <if test="att_file_no_main != null">
	            , att_file_no_main = #{att_file_no_main}
	        </if>
	    WHERE rcp_seq = #{rcp_seq}
	</update>
	 */
	// 레시피 본문 수정 (recipe_manual은 별도 처리)
	public int recipeUpdate(RecipeInsertVO vo);
	
	/*
	 * <delete id="recipeManualDeleteByRcpSeq" parameterType="int">
	    DELETE FROM recipe_manual WHERE rcp_seq = #{rcp_seq}
	</delete>
	 */
	// 조리순서 수정 시, 기존 조리순서 전체 삭제 (재삽입 위해)
	public int recipeManualDeleteByRcpSeq(int rcp_seq);
}
