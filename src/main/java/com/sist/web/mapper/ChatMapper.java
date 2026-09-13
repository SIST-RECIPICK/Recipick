package com.sist.web.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.sist.web.vo.ChatVO;

@Mapper
@Repository
public interface ChatMapper {
	/*
	<!-- 채팅 룸 존재 여부 -->
	<select id="chatRoomExist" resultType="int" parameterType="com.sist.web.vo.ChatVO">
		SELECT COUNT(*)
		FROM chat_room
		WHERE user_id1=#{user_id1} AND user_id2=#{user_id2}
	</select>
	*/
	public int chatRoomExist(@Param("user_id1")int user_id1,@Param("user_id2")int user_id2);
	
	/*
	<!-- 채팅 룸 생성 -->
	<insert id="chatRoomCreate" parameterType="com.sist.web.vo.ChatVO">
		INSERT INTO chat_room VALUE(chat_room_id_seq.nextval,#{user_id1},#{user_id2})
	</insert>
	 */
	public void chatRoomCreate(@Param("user_id1")int user_id1,@Param("user_id2")int user_id2);
	
	/*
	 <!-- 채팅 룸 정보 -->
	<select id="chatRoomData" resultType="com.sist.web.vo.ChatVO" parameterType="com.sist.web.vo.ChatVO">
		SELECT room_id,user_id1,user_id2
		FROM chat_room
		WHERE user_id1=#{user_id1} AND user_id2=#{user_id2}
	</select>
	 */
	public ChatVO chatRoomData(@Param("user_id1")int user_id1,@Param("user_id2")int user_id2);
	
	/*
	<!-- 메세지 저장 -->
	<insert id="chatMessageInsert" parameterType="com.sist.web.vo.ChatVO">
		INSERT INTO chat_message VALUES(chat_message_id_seq.nextval,#{room_id},#{user_id},#{message},SYSDATE)
	</insert>
	 */
	public void chatMessageInsert(ChatVO vo);
	
	/*
	<!-- 룸 목록 리스트 -->
	<select id="chatRoomList" resultType="com.sist.web.vo.ChatVO" parameterType="int">
		SELECT CASE
         	WHEN user_id1 = #{user_id} THEN user_id2
         	WHEN user_id2 = #{user_id} THEN user_id1
       	END AS user_id,
        nickname,
        room_id,
        (SELECT message 
         FROM chat_message 
         WHERE room_id=room_id 
         ORDER BY create_at DESC
		 FETCH FIRST 1 ROW ONLY) 
		as message
		FROM chat_room c
		JOIN users u
		ON u.id = CASE
        	WHEN c.user_id1 = 2 THEN c.user_id2
        	WHEN c.user_id2 = 2 THEN c.user_id1
      	END
		WHERE user_id1 = #{user_id}
  		OR user_id2 = #{user_id}
	</select>
	 */
	public List<ChatVO> chatRoomList(@Param("user_id")int user_id);
	
	/*
	<!-- 메세지 리스트 -->
	<select id="chatMessageList" resultType="com.sist.web.vo.ChatVO" parameterType="int">
		SELECT * FROM chat_message
		WHERE room_id=#{room_id}
		ORDER BY crate_at ASC
	</select>
	 */
	public List<ChatVO> chatMessageList(@Param("room_id")int room_id);
}
