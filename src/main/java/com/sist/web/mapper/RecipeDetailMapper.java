package com.sist.web.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.sist.web.vo.IngredientUnitVO;
import com.sist.web.vo.RecipeManualVO;
import com.sist.web.vo.RecipeVO;

@Mapper
@Repository
public interface RecipeDetailMapper {
	
	/*
	<select id="recipeDetailData" resultType="com.sist.web.vo.RecipeVO" parameterType="int">
		SELECT rcp_seq,rcp_nm,rcp_way2,rcp_pat2,info_wgt,info_eng,info_car,info_pro,info_fat,
		info_na,hash_tag,att_file_no_main,att_file_no_main,att_file_no_mk,rcp_parts_dtls,rcp_na_tip
		FROM recipe
		WHERE rcp_seq=#{rcp_seq}
	</select>
	 */
	public RecipeVO recipeDetailData(int rcp_seq);
	
	/*
	<select id="recipeHowList" resultType="com.sist.web.vo.RecipeManualVO" parameterType="int">
		SELECT rcp_seq,step_no,manual_desc,manual_img
		FROM recipemanual
		WHERE rcp_seq=#{rcp_seq}
	</select>
	 */
	public List<RecipeManualVO> recipeHowList(int rcp_seq);
	
	/*
	<select id="ingredientUnitList" resultType="com.sist.web.vo.IngredientUnitVO" parameterType="int">
		SELECT recipe_id,name,amount,unit,amount_text,original
		FROM Ingredient_unit
		WHERE rcp_seq=#{rcp_seq}
	</select>
	 */
	public List<IngredientUnitVO> ingredientUnitList(int rcp_seq);
}
