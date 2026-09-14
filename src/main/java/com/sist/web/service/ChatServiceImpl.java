package com.sist.web.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sist.web.mapper.ChatMapper;
import com.sist.web.vo.ChatVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService{
	
	private final ChatMapper mapper;
	
	@Override
	public int chatRoomExist(int user_id1, int user_id2) {
		
		return mapper.chatRoomExist(user_id1, user_id2);
	}

	@Override
	public void chatRoomCreate(int user_id1, int user_id2) {
		
		mapper.chatRoomCreate(user_id1, user_id2);
	}

	@Override
	public ChatVO chatRoomData(int user_id1, int user_id2) {
	
		return mapper.chatRoomData(user_id1, user_id2);
	}

	@Override
	public void chatMessageInsert(ChatVO vo) {
		
		mapper.chatMessageInsert(vo);
	}

	@Override
	public List<ChatVO> chatRoomList(int user_id) {

		return mapper.chatRoomList(user_id);
	}

	@Override
	public List<ChatVO> chatMessageList(int room_id) {
		
		return mapper.chatMessageList(room_id);
	}

}
