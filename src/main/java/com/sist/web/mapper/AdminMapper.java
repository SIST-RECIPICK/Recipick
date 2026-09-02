package com.sist.web.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

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
			+ "FROM users ")
	public int userTotalCount();
	
	@Update("UPDATE users "
			+ "SET role = #{role} "
			+ "WHERE id = #{id}")
	public void userRoleUpdate(@Param("id") int id, @Param("role") String role);
	
	@Update("UPDATE users "
			+ "SET status = #{status} "
			+ "WHERE id = #{id}")
	public void userStatusUpdate(@Param("id") int id, @Param("status") String status);
	
}
