package com.sist.web.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import com.sist.web.vo.UsersVO;

@Mapper
@Repository
public interface AdminMapper {
	@Select("SELECT id, nickname, status, role, updated_at "
			+ "FROM users "
			+ "ORDER BY id DESC "
			+ "OFFSET #{start} ROWS FETCH NEXT 15 ROWS ONLY")
	public List<UsersVO> usersList(@Param("start") int start);
}
