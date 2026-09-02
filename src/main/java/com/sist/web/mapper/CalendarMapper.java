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
	/*
	 * <insert id="upsertCalendarItem" parameterType="com.sist.web.vo.CalendarItemVO">
	    MERGE INTO CALENDAR_ITEM ci
	    USING (SELECT #{user_id} AS user_id, #{meal_date} AS meal_date, #{meal_type} AS meal_type FROM dual) src
	    ON (ci.user_id = src.user_id AND ci.meal_date = src.meal_date AND ci.meal_type = src.meal_type)
	    WHEN MATCHED THEN
	        UPDATE SET ci.RCP_SEQ = #{rcp_seq}
	    WHEN NOT MATCHED THEN
	        INSERT (id, user_id, meal_date, meal_type, RCP_SEQ)
	        VALUES (seq_calendar_item.NEXTVAL, #{user_id}, #{meal_date}, #{meal_type}, #{rcp_seq})
	  </insert>
	 */
	public void upsertCalendarItem(CalendarItemVO vo);
}
