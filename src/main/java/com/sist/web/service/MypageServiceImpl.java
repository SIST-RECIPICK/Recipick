package com.sist.web.service;

import java.util.Map;
import org.springframework.stereotype.Service;

import com.sist.web.mapper.MypageMapper;
import com.sist.web.vo.UsersVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MypageServiceImpl implements MypageService {

	private final MypageMapper mMapper;

	@Override
	public UsersVO mypageSideProfile(int userId) {
		// TODO Auto-generated method stub
		return mMapper.mypageSideProfile(userId);
	}

	@Override
	public Map<String, Object> mypageMainCount(int userId) {
		// TODO Auto-generated method stub
		return mMapper.mypageMainCount(userId);
	}

	
}