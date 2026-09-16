package com.sist.web.postgres;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AiFillHistoryMapper {
/*
 * <insert id="insertFillHistory">
        INSERT INTO ai_fill_history (user_id, meal_date, meal_type, rcp_seq)
        VALUES (#{user_id}, TO_DATE(#{meal_date}, 'YYYY-MM-DD'), #{meal_type}, #{rcp_seq})
    </insert>

    <select id="selectRecentFillHistory" resultType="map">
        SELECT user_id, meal_date, meal_type, rcp_seq
        FROM ai_fill_history
        WHERE user_id = #{user_id}
          AND rolled_back = false
        ORDER BY filled_at DESC
    </select>

    <update id="markAsRolledBack">
        UPDATE ai_fill_history
        SET rolled_back = true
        WHERE user_id = #{user_id}
          AND rolled_back = false
    </update>
 */
	// AI가 채운 슬롯 하나를 이력으로 기록
	public void insertFillHistory(
			@Param("user_id") int user_id,
			@Param("meal_date") String meal_date,
			@Param("meal_type") String meal_type,
			@Param("rcp_seq") int rcp_seq
	);
	
	// 아직 롤백 안된 이 사용자의 가장 최근 채우기 기록 조회
	public List<Map<String,Object>> selectRecentFillHistory(
			@Param("user_id") int user_id
	);
	
	public void markAsRolledBack(
			@Param("user_id") int user_id
	);
	
}
