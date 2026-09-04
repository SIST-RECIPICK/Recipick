package com.sist.web.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

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
	
	
}
