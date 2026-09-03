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
/*
 * <select id="selectCalendarInfo" parameterType="map" resultType="com.sist.web.vo.CalendarInfoVO">
    SELECT
        ROUND(SUM(r.INFO_ENG),1) AS total_cal,
        ROUND(SUM(r.INFO_ENG) / COUNT(DISTINCT ci.meal_date),1) AS avg_cal,
        ROUND(COUNT(ci.id) / (#{days_in_month} * 3) * 100,1) AS fill_rate,
        COUNT(ci.id) AS filled_count,
        (#{days_in_month} * 3) as total_slots
        ROUND(SUM(r.INFO_CAR) / COUNT(DISTINCT ci.meal_date),1) AS total_car,
        ROUND(SUM(r.INFO_PRO) / COUNT(DISTINCT ci.meal_date),1) AS total_pro,
        ROUND(SUM(r.INFO_FAT) / COUNT(DISTINCT ci.meal_date),1) AS total_fat
    FROM CALENDAR_ITEM ci
    JOIN RECIPE r ON ci.RCP_SEQ = r.RCP_SEQ
    WHERE ci.user_id = #{user_id}
      AND TO_CHAR(ci.meal_date, 'YYYY') = #{year}
      AND TO_CHAR(ci.meal_date, 'MM') = #{month}   	
  </select>
	  */
	public CalendarInfoVO selectCalendarInfo(
			@Param("user_id") int user_id,
			@Param("year") String year,
			@Param("month") String month,
			@Param("days_in_month") int days_in_month
			);
	/*
	  <select id="selectTop1Recipe" parameterType="map" resultType="map">
	     SELECT * FROM (
	        SELECT 
	        	ci2.RCP_SEQ AS top1_rcp_seq,
	        	r2.RCP_NM AS top1_nm,
	        	COUNT(ci2.RCP_SEQ) AS top1_count
	        FROM CALENDAR_ITEM ci2
	        JOIN RECIPE r2 ON ci2.RCP_SEQ = r2.RCP_SEQ
	        WHERE ci.user_id = #{user_id}
		      AND TO_CHAR(ci.meal_date, 'YYYY') = #{year}
		      AND TO_CHAR(ci.meal_date, 'MM') = #{month} 
	        GROUP BY ci2.RCP_SEQ, r2.RCP_NM
	        ORDER BY top1_count DESC
	    ) WHERE ROWNUM = 1;
	</select>
 */
	// Top1레시피명 조회 따로 분류
	public Map<String, Object> selectTop1Recipe(
			@Param("user_id") int user_id,
	        @Param("year") String year,
	        @Param("month") String month
			);
	
}
