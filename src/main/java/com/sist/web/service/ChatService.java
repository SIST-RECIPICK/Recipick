package com.sist.web.service;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.sist.web.vo.ChatVO;

public interface ChatService {
	public int chatRoomExist(int user_id1,int user_id2);
	public void chatRoomCreate(int user_id1,int user_id2);
	public ChatVO chatRoomData(@Param("user_id1")int user_id1,@Param("user_id2")int user_id2);
	public void chatMessageInsert(ChatVO vo);
	public List<ChatVO> chatRoomList(@Param("user_id")int user_id);
	public List<ChatVO> chatMessageList(@Param("room_id")int room_id);
}
