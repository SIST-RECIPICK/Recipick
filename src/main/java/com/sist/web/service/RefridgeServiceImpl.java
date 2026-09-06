package com.sist.web.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sist.web.mapper.RefridgeMapper;
import com.sist.web.vo.RefridgeVO;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class RefridgeServiceImpl implements RefridgeService{
	private final RefridgeMapper rMapper;
	@Override
	public void registerData(List<RefridgeVO> volist) {
		// TODO Auto-generated method stub
		for(RefridgeVO vo:volist) {
		System.out.println("vo = " + vo);
		rMapper.registerData(vo);
		}
	}
}
