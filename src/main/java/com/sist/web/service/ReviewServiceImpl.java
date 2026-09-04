package com.sist.web.service;

import java.util.*;

import org.springframework.stereotype.Service;

import com.sist.web.mapper.ReviewMapper;
import com.sist.web.vo.Review_BoardVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
	private final ReviewMapper rMapper;
	private final int ROW_SIZE = 12;
	
	@Override
    public Map<String, Object> ReviewBoardListData(int page) {

        int start = (page - 1) * ROW_SIZE;
        List<Review_BoardVO> list = rMapper.ReviewBoardListData(start);

        int totalpage = rMapper.reviewBoardTotalpage();
        final int BLOCK = 12;
        int startpage = ((page - 1) / BLOCK * BLOCK) + 1;
        int endpage = ((page - 1) / BLOCK * BLOCK) + BLOCK;
        if (endpage > totalpage) {
            endpage = totalpage;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("list", list);
        map.put("curpage", page);
        map.put("totalpage", totalpage);
        map.put("startpage", startpage);
        map.put("endpage", endpage);

        return map;
    }

	

}
