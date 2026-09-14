package com.sist.web.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.sist.web.vo.CurationDetailVO;
import com.sist.web.vo.CurationVO;

@Mapper
public interface CommunityMapper {
	
	@Select("SELECT CEIL(COUNT(*)/10.0) "
			+ "FROM curation ")
	public int totalPageCount();
	
	public List<CurationVO> selectCurationList(@Param("start") int start);
	
	@Select("SELECT id, title, year, month, year || '년 '|| month ||'월' as targetday, created_at "
			+ "FROM curation "
			+ "WHERE id = #{id}")
	public CurationVO selectCurationHeader(@Param("id") int id);
		
	public List<CurationDetailVO> selectCurationDetail(@Param("curation_id") int curation_id);
	
	@Update("UPDATE curation SET hit = hit+1 WHERE id = #{id}")
	public void updateCurationHit(@Param("id") int id);
	
}
