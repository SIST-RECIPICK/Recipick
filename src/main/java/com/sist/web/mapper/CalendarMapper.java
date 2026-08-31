package com.sist.web.mapper;
import java.util.*;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.sist.web.vo.*;
@Mapper
@Repository
public interface CalendarMapper {
	/*
	 * <select id="selectCanlendarItems">
	  	SELECT ci.id AS id,
	  		   ci.user_id as user_id,
	  		   ci.meal_date as meal_date,
	  		   ci.meal_type as meal_type,
	  		   ci.RCP_SEQ as rcp_seq,
	  		   ci.RCP_NM as rcp_nm
	  		   FROM CALENDAR_ITEM ci
	  		   JOIN RECIPE r ON ci.RCP_SEQ = r.RCP_SEQ
	  		   WHERE ci.user_id = #{user_id}
	  		     AND TO_CHAR(ci.meal_date, 'YYYY') = #{year}
	  		     AND TO_CHAR(ci.meal_date, 'MM') = #{month}
	  		   ORDER BY ci.meal_date, ci.meal_type
	  </select>
	 */
	public List<CalendarItemVO> selectCalendarItems(
			@Param("user_id") int user_id,
			@Param("year") String year,
			@Param("month") String month
	);
}
