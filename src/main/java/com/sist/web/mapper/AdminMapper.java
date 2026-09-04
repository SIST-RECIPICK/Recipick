package com.sist.web.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import com.sist.web.vo.CurationDetailVO;
import com.sist.web.vo.CurationVO;
import com.sist.web.vo.UsersVO;

@Mapper
@Repository
public interface AdminMapper {
	@Select("SELECT id, nickname, status, role, created_at "
			+ "FROM users "
			+ "ORDER BY id DESC "
			+ "OFFSET #{start} ROWS FETCH NEXT 15 ROWS ONLY")
	public List<UsersVO> usersList(@Param("start") int start);
	
	@Select("SELECT CEIL(COUNT(*)/10.0) "
			+ "FROM ${tablename} ")
	public int totalPageCount(String tablename);
	
	@Update("UPDATE users "
			+ "SET role = #{role} "
			+ "WHERE id = #{id}")
	public void userRoleUpdate(@Param("id") int id, @Param("role") String role);
	
	@Update("UPDATE users "
			+ "SET status = #{status} "
			+ "WHERE id = #{id}")
	public void userStatusUpdate(@Param("id") int id, @Param("status") String status);
	
	public List<CurationVO> selectCurationList(@Param("start") int start);
	
	@Select("SELECT id, title, year || '년 '|| month ||'월' as targetday, created_at "
			+ "FROM curation "
			+ "WHERE id = #{id}")
	public CurationVO selectCurationHeader(@Param("id") int id);
	
	public List<CurationDetailVO> selectCurationDetail(@Param("curation_id") int curation_id);

	@Delete("DELETE FROM CURATION WHERE id = #{id}")
	public void deleteCuration(int id);
	
	@Delete("DELETE FROM CURATION_DETAIL WHERE curation_id = #{id}")
	public void deleteCurationDetail(int id);
	
	
}
