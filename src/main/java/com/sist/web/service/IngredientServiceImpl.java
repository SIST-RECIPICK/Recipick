package com.sist.web.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sist.web.mapper.IngredientMapper;
import com.sist.web.vo.IngredientVO;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class IngredientServiceImpl implements IngredientService{
	private final IngredientMapper iMapper;

	@Override
	public List<IngredientVO> searchData(String keyword) {
		// TODO Auto-generated method stub
		return iMapper.searchData(keyword);
	}

}
