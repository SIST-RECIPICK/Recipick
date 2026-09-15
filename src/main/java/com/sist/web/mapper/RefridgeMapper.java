package com.sist.web.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.sist.web.vo.RecipeVO;
import com.sist.web.vo.RefridgeVO;
@Mapper
@Repository
public interface RefridgeMapper {
   /*
    *  <insert id="registerData" parameterType="com.sist.vo.RefridgeVO">
	    INSERT INTO Refrigerator(id,users_id,ingredient_id,created_at)
	    VALUES(ref_id_seq.nextval,#{users_id},#{ingredient_id},SYSDATE)
	  </insert>
    * 
    * 
    */
	public void registerData(RefridgeVO vo);
	
	
	/*
		 * <select id="fridgeData" parameterType="int" resultMap="refridgeResultMap">
	        SELECT r.id, r.users_id, r.ingredient_id, r.created_at,
	               i.id AS ing_id, i.category_name, i.ingredient_name
	        FROM Refrigerator r
	        JOIN ingredient i ON r.ingredient_id = i.id
	        WHERE r.users_id = #{users_id}
	       </select>
	 * 
	 * 
	 */
	public List<RefridgeVO> fridgeData(int user_id);
	
	
	/*
	    * <select id="oracleRecipeAllData" resultType="com.sist.web.vo.RecipeVO">
	       SELECT * FROM recipe
	       ORDER BY rcp_seq
	   		</select>
	    * 
	    */
		//public RecipeVO oracleRecipeAllData(int rcp_seq);
}
