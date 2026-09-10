package com.sist.web.service;

import java.util.List;
import java.util.Map;

import com.sist.web.vo.CurationCreateVO;
import com.sist.web.vo.CurationVO;
import com.sist.web.vo.RecipeVO;

public interface CommunityService {
	
	public int[] pages(int page);
	
	public List<CurationVO> curation_list(int page);
	
	public CurationVO selectCurationDetail(int id);

}
